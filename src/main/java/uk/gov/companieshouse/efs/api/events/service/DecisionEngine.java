package uk.gov.companieshouse.efs.api.events.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filetransfer.AvStatus;
import uk.gov.companieshouse.api.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.filetransfer.FileTransferService;
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

    private final FileTransferService fileTransferService;
    private final FormTemplateRepository repository;
    private final CurrentTimestampGenerator timestampGenerator;
    private final SubmissionService submissionService;

    public DecisionEngine(final FileTransferService fileTransferService, final FormTemplateRepository repository,
                          final CurrentTimestampGenerator timestampGenerator, final SubmissionService submissionService) {
        this.fileTransferService = fileTransferService;
        this.repository = repository;
        this.timestampGenerator = timestampGenerator;
        this.submissionService = submissionService;
    }

    public Map<DecisionResult, List<Decision>> evaluateSubmissions(final List<Submission> submissions) {
        return submissions.stream()
                .map(this::evaluate)
                .collect(Collectors.groupingBy(Decision::getDecisionResult));
    }

    private Decision evaluate(final Submission submission) {
        final var decision = new Decision(submission);
        for (final var fileDetails : submission.getFormDetails().getFileDetailsList()) {
            if (fileDetails.getConversionStatus() == FileConversionStatus.WAITING) {
                checkAvStatus(submission.getId(), decision, fileDetails);
            } else {
                decision.incrementNumberOfDecisions();
            }
        }
        if (decision.isChanged()) {
            submissionService.updateSubmission(decision.getSubmission());
        }
        return finaliseDecision(decision, submission.getFormDetails().getFormType());
    }

    private void checkAvStatus(final String submissionId, final Decision decision, final FileDetails fileDetails) {
        LOGGER.debug("Checking AV status for file with id [%s] in submission [%s]".formatted(fileDetails.getFileId(), submissionId));
        fileTransferService.getFileDetails(fileDetails.getFileId())
            .map(FileDetailsApi::getAvStatus)
            .map(AvStatus::getValue)
            .ifPresent(avStatus -> handleAvStatus(avStatus, decision, fileDetails));
    }

    private void handleAvStatus(final String avStatusValue, final Decision decision, final FileDetails fileDetails) {
        FileConversionStatus status = null;
        var infected = false;

        if (AV_FAILED.equals(avStatusValue)) {
            status = FileConversionStatus.FAILED_AV;
            infected = true;
        } else if (AV_CLEAN.equals(avStatusValue)) {
            status = FileConversionStatus.CLEAN_AV;
        }
        if (status != null) {
            fileDetails.setConversionStatus(status);
            fileDetails.setLastModifiedAt(timestampGenerator.generateTimestamp());
            if (infected) {
                decision.addInfectedFile(fileDetails.getFileName());
            }
            decision.setChanged(true);
            decision.incrementNumberOfDecisions();
        }
    }

    private Decision finaliseDecision(final Decision decision, final String formType) {
        return repository.findById(formType)
            .map(template -> {
                if (decision.getExpectedDecisions() != decision.getNumberOfDecisions()) {
                    decision.setDecisionResult(DecisionResult.NO_DECISION);
                } else if (decision.containsInfectedFile()) {
                    decision.setDecisionResult(DecisionResult.NOT_CLEAN);
                } else if (template.isFesEnabled()) {
                    decision.setDecisionResult(DecisionResult.FES_ENABLED);
                } else {
                    decision.setDecisionResult(DecisionResult.NOT_FES_ENABLED);
                }
                return decision;
            })
            .orElseGet(() -> {
                decision.setDecisionResult(DecisionResult.FORM_TYPE_DOES_NOT_EXIST);
                return decision;
            });
    }
}
