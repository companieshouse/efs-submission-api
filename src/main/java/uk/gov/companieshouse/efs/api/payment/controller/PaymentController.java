package uk.gov.companieshouse.efs.api.payment.controller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionApiMapper;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;

@RestController
@RequestMapping("/efs-submission-api/submission/")
public class PaymentController {
    private static final ImmutableSet<SubmissionStatus> VALID_STATUSES =
        Sets.immutableEnumSet(SubmissionStatus.OPEN, SubmissionStatus.PAYMENT_REQUIRED);

    private final Logger logger;
    private final Clock clock;

    private final SubmissionService submissionService;
    private final FormTemplateService formTemplateService;
    private final PaymentTemplateService paymentTemplateService;
    private final EmailService emailService;
    private final SubmissionApiMapper submissionApiMapper;

    @Autowired
    public PaymentController(final SubmissionService submissionService,
        final FormTemplateService formTemplateService,
        final PaymentTemplateService paymentTemplateService, final EmailService emailService,
        final SubmissionApiMapper submissionApiMapper, final Clock clock, final Logger logger) {
        this.formTemplateService = formTemplateService;
        this.paymentTemplateService = paymentTemplateService;
        this.submissionService = submissionService;
        this.emailService = emailService;
        this.submissionApiMapper = submissionApiMapper;
        this.clock = clock;
        this.logger = logger;
    }

    /**
     * Return payment details requested by CHS Payment Service as part of the payment journey.
     *
     * @param id      the submission ID
     * @param request the HTTP request
     * @return the payment template details
     */
    @GetMapping(value = "{id}/payment", produces = {"application/json"})
    public ResponseEntity<PaymentTemplate> getPaymentDetails(@PathVariable("id") final String id,
        final HttpServletRequest request) {

        logger.debug(MessageFormat.format("Fetching submission with id: {0}", id));

        final SubmissionApi submission = submissionService.readSubmission(id);
        final ResponseEntity<PaymentTemplate> response;

        if (submission == null) {
            response = ResponseEntity.notFound().build();
        } else if (submission.getSubmissionForm() == null || StringUtils
            .isBlank(submission.getSubmissionForm().getFormType())) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            String formType = submission.getSubmissionForm().getFormType();

            logger.debug(MessageFormat.format("Fetching form type with id: {0}", formType));
            FormTemplateApi formTemplate = formTemplateService.getFormTemplate(formType);
            if (formTemplate != null) {
                final String chargeTemplateId = formTemplate.getPaymentCharge();
                final LocalDateTime now = LocalDateTime.now(clock);

                logger.debug(MessageFormat.format("Fetching template for fee: {0} at {1}",
                    chargeTemplateId, now));
                if (StringUtils.isNotBlank(chargeTemplateId)) {
                    final Optional<PaymentTemplate> optionalTemplate =
                        paymentTemplateService.getTemplate(chargeTemplateId, now);

                    optionalTemplate.ifPresent(t -> logger.debug(MessageFormat.format("template={0}", t)));
                    response = optionalTemplate
                        .map(paymentTemplate -> getPaymentTemplateResponse(id, request, paymentTemplate))
                        .orElseGet(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)::build);
                    return response;
                } else {
                    response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return response;
    }

    /**
     * Process payment patch request by CHS Payment Service after payment journey has completed.
     *
     * @param id      the submission ID
     * @param request the HTTP request
     * @return the payment template details
     */
    @PatchMapping(value = "{id}/payment", produces = {"application/json"})
    public ResponseEntity<SubmissionResponseApi> patchPaymentSession(
        @PathVariable("id") final String id, @RequestBody PaymentClose paymentClose,
        final HttpServletRequest request) {

        final Map<String, Object> debug = new HashMap<>();

        debug.put("submissionId", id);
        debug.put("paymentClose.paymentReference", paymentClose.getPaymentReference());
        debug.put("paymentClose.paidAt", paymentClose.getPaidAt());
        debug.put("paymentClose.status", paymentClose.getStatus());
        logger.debug("PATCH payment", debug);
        
        logger.debug(MessageFormat.format("Fetching submission with id: {0}", id));

        final SubmissionApi submission = submissionService.readSubmission(id);
        ResponseEntity<SubmissionResponseApi> response = ResponseEntity.noContent()
            .build();

        if (submission == null) {
            response = ResponseEntity.notFound().build();
        } else if (!VALID_STATUSES.contains(submission.getStatus())) {
            response = ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            final SubmissionStatus oldStatus = submission.getStatus();

            try {
                submissionService.updateSubmissionWithPaymentOutcome(id, paymentClose);
                if (oldStatus == SubmissionStatus.PAYMENT_REQUIRED) {
                    if (paymentClose.isPaid()) {
                        emailService.sendExternalConfirmation(new ExternalNotificationEmailModel(
                            submissionApiMapper.map(submission)));

                    } else if (paymentClose.isFailed()){
                        emailService.sendExternalPaymentFailedNotification(
                            new ExternalNotificationEmailModel(
                                submissionApiMapper.map(submission)));
                    }
                }
            } catch (SubmissionIncorrectStateException e) {
                response = ResponseEntity.badRequest().build();
            }
        }

        return response;
    }

    private ResponseEntity<PaymentTemplate> getPaymentTemplateResponse(final String id,
        final HttpServletRequest request, final PaymentTemplate paymentTemplate) {
        final ResponseEntity<PaymentTemplate> response;
        final SubmissionApi submission = submissionService.readSubmission(id);

        if (submission.getCompany() == null || StringUtils.isBlank(submission.getCompany().getCompanyNumber())) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            final String companyNumber = submission.getCompany().getCompanyNumber();
            try {
                URI selfUri = new URI(StringUtils.removeEnd(request.getRequestURL().toString(), "/"));
                URI parentUri = selfUri.resolve(".");
                URL resourceUrl = parentUri.toURL();

                paymentTemplate.setLinks(
                    new PaymentTemplate.Links(StringUtils.removeEnd(resourceUrl.toString(), "/"), selfUri.toURL()));
                paymentTemplate.setCompanyNumber(companyNumber);

            } catch (URISyntaxException | MalformedURLException e) {
                final String thisMethod = new Throwable().getStackTrace()[0].getMethodName();

                logger.errorContext(id, thisMethod + ": " + e.getMessage(), e, null);

                return ResponseEntity.badRequest().build();
            }

            response = ResponseEntity.ok().body(paymentTemplate);
        }
        return response;
    }

    /**
     * Endpoint for payment sessions update.
     *
     * @param id              submission id
     * @param paymentSessions payment sessions
     * @param result          bindingResult
     * @return ResponseEntity&lt;SubmissionResponseApi&gt;
     */
    @PutMapping("{id}/payment-sessions")
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
            return ResponseEntity.ok(
                submissionService.updateSubmissionWithPaymentSessions(id, paymentSessions));
        } catch (SubmissionNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (SubmissionIncorrectStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }
}
