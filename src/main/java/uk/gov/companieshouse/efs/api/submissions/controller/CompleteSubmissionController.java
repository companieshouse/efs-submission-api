package uk.gov.companieshouse.efs.api.submissions.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}")
public class CompleteSubmissionController {

    private SubmissionService service;

    public CompleteSubmissionController(SubmissionService service) {
        this.service = service;
    }

    /**
     * Endpoint to complete submission.
     *
     * @param   id  submission id
     * @return      ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> completeSubmission(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.completeSubmission(id));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (SubmissionValidationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
