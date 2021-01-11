package uk.gov.companieshouse.efs.api.events.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService submissionService;
    private MessageService messageService;
    private EmailService emailService;
    private S3ClientService s3ClientService;
    private String fileBucketName;

    @Autowired
    public ExecutionEngine(SubmissionService submissionService, MessageService messageService, EmailService emailService, S3ClientService s3ClientService,
        @Qualifier("file-bucket-name") String fileBucketName) {
        this.submissionService = submissionService;
        this.messageService = messageService;
        this.emailService = emailService;
        this.s3ClientService = s3ClientService;
        this.fileBucketName = fileBucketName;
    }

    public void execute(Map<DecisionResult, List<Decision>> decisionGroups) {
        handleNoDecision(decisionGroups.getOrDefault(DecisionResult.NO_DECISION, Collections.emptyList()));
        handleInvalidFormType(decisionGroups.getOrDefault(DecisionResult.FORM_TYPE_DOES_NOT_EXIST, Collections.emptyList()));
        handleInfectedFiles(decisionGroups.getOrDefault(DecisionResult.NOT_CLEAN, Collections.emptyList()));
        handleFesEnabledForms(decisionGroups.getOrDefault(DecisionResult.FES_ENABLED, Collections.emptyList()));
        handleNonFesEnabledForms(decisionGroups.getOrDefault(DecisionResult.NOT_FES_ENABLED, Collections.emptyList()));
    }

    private void handleNoDecision(List<Decision> decisions) {
        decisions.forEach(decision ->
                LOGGER.debug(String.format(
                        "Submission with id: [%s] contains files still awaiting virus scan or encountered errors in file-transfer-api",
                        decision.getSubmission().getId()))
        );
    }

    private void handleInvalidFormType(List<Decision> decisions) {
        decisions.forEach(decision ->
                LOGGER.error(String.format("Submission with id: [%s] has unhandled form type [%s]",
                        decision.getSubmission().getId(), decision.getSubmission().getFormDetails().getFormType()))
        );
    }

    private void handleInfectedFiles(List<Decision> decisions) {
        decisions.forEach(decision -> {
            this.emailService.sendInternalFailedAV(new InternalAvFailedEmailModel(decision.getSubmission(), decision.getInfectedFiles()));
            this.submissionService.updateSubmissionStatus(decision.getSubmission().getId(), SubmissionStatus.REJECTED_BY_VIRUS_SCAN);
        });
    }

    private void handleFesEnabledForms(List<Decision> decisions) {
        if (!decisions.isEmpty()) {
            this.messageService.queueMessages(decisions);
            decisions.forEach(decision -> submissionService.updateSubmissionQueued(decision.getSubmission()));
        }
    }

    private void handleNonFesEnabledForms(List<Decision> decisions) {
        decisions.forEach(this::sendInternalEmail);
    }

    private void sendInternalEmail(final Decision decision) {

        Submission submission = decision.getSubmission();
        InternalSubmissionEmailModel emailModel = new InternalSubmissionEmailModel(submission,
                createEmailFileDetailsList(submission.getFormDetails().getFileDetailsList()));
        emailService.sendInternalSubmission(emailModel);
        submissionService.updateSubmissionStatus(submission.getId(), SubmissionStatus.PROCESSED_BY_EMAIL);
        LOGGER.debug(String.format("Processed submission [%s] by email", submission.getId()));
    }

    private List<EmailFileDetails> createEmailFileDetailsList(List<FileDetails> fileDetails) {
        return fileDetails.stream()
                .map(file -> new EmailFileDetails(file, s3ClientService.generateFileLink(file.getFileId(), fileBucketName)))
                .collect(Collectors.toList());
    }
}
