package uk.gov.companieshouse.efs.api.payment.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.logging.Logger;

@RestController
@ResponseStatus(HttpStatus.OK)
@RequestMapping(value = "/efs-submission-api/payment-templates", produces = {"application/json"})

public class PaymentControllerSpike {
    private static final Instant CHARGE_TIMESTAMP = Instant.parse("2019-01-08T00:00:00.000Z");
    private final Logger logger;
    private final PaymentTemplateService paymentTemplateService;

    @Autowired
    public PaymentControllerSpike(final PaymentTemplateService paymentTemplateService,
            Logger logger) {
        this.paymentTemplateService = paymentTemplateService;
        this.logger = logger;
    }

    /**
     * Returns a responseEntity which contains a list of payment templates belonging to an optional
     * form category, or all templates if category is omitted. Will return a status of not found if
     * the category is not found.
     *
     * @return responseEntity
     */
    @GetMapping
    public ResponseEntity<PaymentTemplate> getPaymentTemplates(
            @RequestParam(value = "type") String id, HttpServletRequest request) {

        Map<String, Object> info = new HashMap<>();
        info.put("id", id);
        info.put("chargedAt", CHARGE_TIMESTAMP);
        logger.info("Payment template request for ", info);

        try {
            final Optional<PaymentTemplate> template =
                    paymentTemplateService.getTemplate(id, CHARGE_TIMESTAMP);

            logger.info("Payment template retrieved " + template.map(PaymentTemplate::getId)
                    .map(PaymentTemplateId::getFee)
                    .orElse("nothing found"));

            return ResponseEntity.of(template);
        }
        catch (Exception ex) {

            logger.error("Failed to get payment templates", ex, info);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }


    @GetMapping("/test")
    public ResponseEntity<Void> createTestPaymentTemplate() {

        PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder()
                .withId(new PaymentTemplateId("TEST", Instant.now()))
                .withDescription("Testing")
                .build();

        paymentTemplateService.putTemplate(paymentTemplate);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}