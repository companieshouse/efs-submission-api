package uk.gov.companieshouse.efs.api.events.service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.filetransfer.FileTransferApiClient;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DecisionEngine {

    private static final String AV_FAILED = "infected";
    private static final String AV_CLEAN = "clean";
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private FileTransferApiClient client;
    private FormTemplateRepository repository;
    private CurrentTimestampGenerator timestampGenerator;
    private SubmissionService submissionService;

    /**
     * Constructor.
     *
     * @param client                dependency
     * @param repository            dependency
     * @param timestampGenerator    dependency
     * @param submissionService     dependency
     */
    public DecisionEngine(FileTransferApiClient client, FormTemplateRepository repository,
                          CurrentTimestampGenerator timestampGenerator, SubmissionService submissionService) {
        this.client = client;
        this.repository = repository;
        this.timestampGenerator = timestampGenerator;
        this.submissionService = submissionService;
    }

    public Map<DecisionResult, List<Decision>> evaluateSubmissions(List<Submission> submissions) {
        return submissions.stream()
                .map(this::evaluate)
                .collect(Collectors.groupingBy(Decision::getDecisionResult));
    }

    private Decision evaluate(Submission submission) {
        Decision decision = new Decision(submission);
        // Loop through all files inside a submission
        // then assign a FileConversionStatus if it's been virus scanned
        for (FileDetails fileDetails : submission.getFormDetails().getFileDetailsList()) {
            if (fileDetails.getConversionStatus() == FileConversionStatus.WAITING) {
                checkAvStatus(submission.getId(), decision, fileDetails);
            } else {
                decision.incrementNumberOfDecisions();
            }
        }
        if (decision.isChanged()) {
            submissionService.updateSubmission(decision.getSubmission());
        }
        return decide(decision, submission.getFormDetails().getFormType());
    }

    private void checkAvStatus(String submissionId, Decision decision, FileDetails fileDetails) {
        FileTransferApiClientDetailsResponse response = this.client.details(fileDetails.getFileId());
        if (response.getHttpStatus() != HttpStatus.OK) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", submissionId);
            debug.put("fileId", fileDetails.getFileId());
            debug.put("status", response.getHttpStatus());

            LOGGER.errorContext(submissionId, String.format(
                    "File transfer API returned status [%s] for file id [%s] in submission [%s]",
                    response.getHttpStatus(), fileDetails.getFileId(), submissionId), null, debug);
        } else if (AV_FAILED.equals(response.getFileStatus())) {
            fileDetails.setConversionStatus(FileConversionStatus.FAILED_AV);
            fileDetails.setLastModifiedAt(timestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime());
            decision.addInfectedFile(fileDetails.getFileName());
            decision.setChanged(true);
            decision.incrementNumberOfDecisions();
        } else if (AV_CLEAN.equals(response.getFileStatus())) {
            fileDetails.setConversionStatus(FileConversionStatus.CLEAN_AV);
            fileDetails.setLastModifiedAt(timestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime());
            decision.setChanged(true);
            decision.incrementNumberOfDecisions();
        }
    }

    private Decision decide(Decision decision, String formType) {
        Optional<FormTemplate> formTemplate = repository.findById(formType);
        if (decision.getExpectedDecisions() != decision.getNumberOfDecisions()) {
            decision.setDecisionResult(DecisionResult.NO_DECISION);
        } else if (decision.containsInfectedFile()) {
            decision.setDecisionResult(DecisionResult.NOT_CLEAN);
        } else if (!formTemplate.isPresent()) {
            decision.setDecisionResult(DecisionResult.FORM_TYPE_DOES_NOT_EXIST);
        } else if (formTemplate.get().isFesEnabled()) {
            decision.setDecisionResult(DecisionResult.FES_ENABLED);
        } else {
            decision.setDecisionResult(DecisionResult.NOT_FES_ENABLED);
        }
        return decision;
    }
}
