package uk.gov.companieshouse.efs.api.submissions.controller;

import com.google.common.collect.ImmutableSet;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/efs-submission-api/submission/")
public class PendStatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private static final ImmutableSet<SubmissionStatus> PENDABLE_STATUSES
        = ImmutableSet.of(SubmissionStatus.OPEN, SubmissionStatus.PAYMENT_REQUIRED, SubmissionStatus.PAYMENT_FAILED);

    private SubmissionService submissionService;

    @Autowired
    public PendStatusController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PutMapping("{id}/pend")
    public ResponseEntity<SubmissionResponseApi> submitPendingPaymentStatus(@PathVariable("id") final String id,
                                                     final HttpServletRequest request) {

        final SubmissionApi submission = submissionService.readSubmission(id);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        if (PENDABLE_STATUSES.contains(submission.getStatus())) {
            SubmissionResponseApi submissionResponseApi = null;
            if (submission.getStatus() != SubmissionStatus.PAYMENT_REQUIRED) {
                submissionResponseApi = submissionService.updateSubmissionStatus(id, SubmissionStatus.PAYMENT_REQUIRED);
                LOGGER.debug("Updated submission status to PAYMENT_REQUIRED.");
            }
            return ResponseEntity.ok(submissionResponseApi);
        }

        LOGGER.debug("Status not updated as submission status is invalid.");
        return ResponseEntity.status(HttpStatus.CONFLICT).build();

    }
}
