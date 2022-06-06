package uk.gov.companieshouse.efs.api.submissions.controller;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}/confirmAuthorised")
public class ConfirmAuthorisedController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private SubmissionService service;

    public ConfirmAuthorisedController(SubmissionService service) {
        this.service = service;
    }

    /**
     * Endpoint to for Confirm Authorised detail.
     *
     * @param id                submission id
     * @param confirmAuthorised confirmAuthorised request
     * @param result            bindingResult
     * @return                  ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> submitAuthorised(@PathVariable String id,
        @RequestBody ConfirmAuthorisedApi confirmAuthorised, BindingResult result) {

        if (result.hasErrors()) {
            String message = Optional.ofNullable(result.getFieldError()).map(e -> e.getDefaultMessage())
                    .orElse(confirmAuthorised.getConfirmAuthorised().toString());
            LOGGER.info(String.format("Presenter must confirm authorisation: %s", message));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(service.updateSubmissionConfirmAuthorised(id, confirmAuthorised.getConfirmAuthorised()));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }
}
