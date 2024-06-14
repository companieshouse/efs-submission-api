package uk.gov.companieshouse.efs.api.payment.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.logging.Logger;

/**
 * Testing/diagnostic endpoints for activation-date -based payment templates. Only instantiated when
 * env var {@code FEATURE_TESTING_FEES=true} (default is false)
 */
@RestController
@ConditionalOnProperty(prefix = "feature", name = "testing-fees", havingValue = "true")
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/efs-submission-api/payment-templates")
public class PaymentFeatureTestingFeesController {
    private static final LocalDateTime CHARGE_TIMESTAMP = LocalDateTime.parse("2019-01-08T00:00:00");
    private final Logger logger;
    private final PaymentTemplateService paymentTemplateService;

    @Autowired
    public PaymentFeatureTestingFeesController(final PaymentTemplateService paymentTemplateService,
            Logger logger) {
        this.paymentTemplateService = paymentTemplateService;
        this.logger = logger;
    }

    /**
     * Returns a responseEntity which contains a list of {@link PaymentTemplate} belonging to an
     * optional
     * fee id, and also that is active at an optional local date/time, or all
     * {@link PaymentTemplate} otherwise.
     * @param fee (optional unless {@code activeAt} is not omitted) the fee id associated with a
     *            form template
     * @param activeAt (optional) a {@link LocalDateTime} at which that the resultant
     * {@link PaymentTemplate} is active
     *
     * @return responseEntity containing list of {@link PaymentTemplate}. Will consist of a
     * single value if {@code fee} and {@code activeAt} are specified.
     */
    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<PaymentTemplate>> getPaymentTemplates(
        @RequestParam(value = "type", required = false) String fee,
        @RequestParam(value = "activeAt", required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime activeAt,
        HttpServletRequest request) {

        Map<String, Object> info = new HashMap<>();
        info.put("fee", fee);
        info.put("chargedAt", CHARGE_TIMESTAMP);
        logger.info("Payment template request for ", info);

        try {
            if (fee == null) {
                return ResponseEntity.ok(paymentTemplateService.getPaymentTemplates());
            }
            if (activeAt == null) {
                final List<PaymentTemplate> template = paymentTemplateService.getPaymentTemplates(
                    fee);
                return ResponseEntity.ok(template);
            }
            return ResponseEntity.ok(paymentTemplateService.getPaymentTemplate(fee, activeAt)
                .map(Arrays::asList)
                .orElseGet(Collections::emptyList));
        }
        catch (Exception ex) {
            Map<String, Object> debug = new HashMap<>();

            debug.put("fee", fee);
            debug.put("activeAt", activeAt);
            logger.error("Failed to get payment template(s)", ex, debug);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DTO class used as request body by {@link #createPaymentTemplate}
     */
    public static class TestingFeesPaymentTemplate {
        final String fee;
        final LocalDateTime activeFrom;
        final String amount;

        @JsonCreator
        public TestingFeesPaymentTemplate(@JsonProperty("fee") final String fee,
            @JsonProperty("active_from") final LocalDateTime activeFrom,
            @JsonProperty("amount") final String amount) {
            this.fee = fee;
            this.activeFrom = activeFrom;
            this.amount = amount;
        }

        public String getFee() {
            return fee;
        }

        @JsonProperty("active_from")
        public LocalDateTime getActiveFrom() {
            return activeFrom;
        }

        public String getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TestingFeesPaymentTemplate.class.getSimpleName() + "[", "]")
                .add("fee='" + fee + "'")
                .add("activeFrom=" + activeFrom)
                .add("amount='" + amount + "'")
                .toString();
        }
    }

    /**
     * Create and store a bare-bones payment template for test purposes.
     * Details required in request body (a {@link TestingFeesPaymentTemplate} DTO):
     * <ul>
     *     <li>Fee ID</li>
     *     <li>ActiveFrom local date/time</li>
     *     <li>Amount</li>
     * </ul>
     *
     * @param templateDetails the DTO containing details to be stored
     *
     * @return the {@link PaymentTemplate} as stored
     */
    @PostMapping(value = "/test-fee", consumes = {"application/json"},
        produces = {"application/json"})
    public ResponseEntity<PaymentTemplate> createPaymentTemplate(
        @RequestBody final TestingFeesPaymentTemplate templateDetails) {

        final PaymentTemplate.Item item = PaymentTemplate.Item.newBuilder()
            .withAmount(templateDetails.getAmount())
            .build();
        final PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder()
            .withId(
                new PaymentTemplateId(templateDetails.getFee(), templateDetails.getActiveFrom()))
            .withItem(item)
            .build();

        final PaymentTemplate saved = paymentTemplateService.postTemplate(paymentTemplate);

        final URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();

        return ResponseEntity.created(location).body(saved);
    }
}