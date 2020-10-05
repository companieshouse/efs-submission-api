package uk.gov.companieshouse.efs.api.fes.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.fes.service.exception.ChipsServiceException;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class FesServiceImpl implements FesService {

    private SubmissionRepository repository;

    private EmailService emailService;

    private ChipsService chipsService;

    private SubmissionService submissionService;

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");
    private static final String SUBMISSION_NOT_FOUND = "Submission not found for barcode [%s]";
    private static final String SUBMISSION_INVALID_STATUS = "Submission [%s] has invalid status: [%s]";

    @Autowired
    public FesServiceImpl(SubmissionRepository repository, EmailService emailService, ChipsService chipsService, SubmissionService submissionService) {
        this.repository = repository;
        this.emailService = emailService;
        this.chipsService = chipsService;
        this.submissionService = submissionService;
    }

    @Override
    public Submission getSubmissionForBarcode(String barcode) {
        return repository.readByBarcode(barcode);
    }

    @Override
    public void updateSubmissionStatusByBarcode(String barcode, FesSubmissionStatus status) {

        // check the status of the submission
        Submission submission = getSubmissionForBarcode(barcode);

        // check if submission exists
        if (submission == null) {
            LOGGER.info(String.format(SUBMISSION_NOT_FOUND, barcode));
            throw new SubmissionNotFoundException(String.format(String.format(SUBMISSION_NOT_FOUND, barcode)));

        } else if (!submission.getStatus().equals(SubmissionStatus.SENT_TO_FES)) {
            LOGGER.info(String.format(SUBMISSION_INVALID_STATUS, submission.getId(), submission.getStatus()));
            throw new SubmissionIncorrectStateException(String.format(SUBMISSION_INVALID_STATUS, submission.getId(), submission.getStatus()));
        }

        // update submission status
        submission.setStatus(SubmissionStatus.valueOf(status.toString()));

        //send Email
        try {
            if (status.equals(FesSubmissionStatus.REJECTED)) {
                List<String> rejectReasons = getRejectReasons(barcode, submission.getId());
                submission.setChipsRejectReasons(
                        rejectReasons.stream().map(RejectReason::new).collect(Collectors.toList()));

                emailService.sendExternalReject(new ExternalRejectEmailModel(submission, rejectReasons));
            } else {
                emailService.sendExternalAccept(new ExternalAcceptEmailModel(submission));
            }
        } catch (EmailServiceException ex) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", submission.getId());
            debug.put("barcode", submission.getFormDetails().getBarcode());
            debug.put("reason", ex.getMessage());

            LOGGER.errorContext(submission.getId(), "Error sending email for submission",
                    null, debug);
        }

        submissionService.updateSubmission(submission);
    }

    private List<String> getRejectReasons(String barcode, String submissionId) {
        List<String> rejectReasons = new ArrayList<>();
        try {
            rejectReasons = chipsService.getRejectReasons(barcode);
        } catch (ChipsServiceException ex) {
            // For a ChipsServiceException log error but still send the external email
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", submissionId);
            debug.put("barcode", barcode);
            debug.put("reason", ex.getMessage());

            LOGGER.errorContext(submissionId,
                    "Error reading reject reasons for submission", null, null);
        }
        return rejectReasons;
    }
}
