package uk.gov.companieshouse.efs.api.payment.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/efs-submission-api/submission/{id}/payment-sessions")
public class PaymentController {

    private Logger logger;

    private SubmissionService service;

    @Autowired
    public PaymentController(SubmissionService service, Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    /**
     * Endpoint for payment sessions update.
     *
     * @param id        submission id
     * @param paymentSessions   payment sessions
     * @param result    bindingResult
     * @return          ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping
    public ResponseEntity<SubmissionResponseApi> submitPaymentSessions(@PathVariable String id,
            @RequestBody @Valid @NotNull SessionListApi paymentSessions, BindingResult result) {

        if (result.hasErrors()) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("submissionId", id);
            debug.put("fieldError", Optional.ofNullable(result.getFieldError())
                .map(FieldError::getDefaultMessage).orElse("Unable to get field error"));
            logger.errorContext(id, "Payment session details are invalid", null, debug);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(service.updateSubmissionWithPaymentSessions(id, paymentSessions));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }
}
