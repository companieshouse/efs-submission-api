package uk.gov.companieshouse.efs.api.payment.service;


import java.time.Instant;
import java.util.Optional;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;

/**
 * Stores and retrieves the payment template information
 */
public interface PaymentTemplateService {

    /**
     * Retrieve the template for the specified id
     *
     * @param fee the payment template id
     * @return the payment template
     */
    default Optional<PaymentTemplate> getTemplate(String fee, Instant chargedAt) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Store the payment template
     *
     * @param template the payment template to be stored
     */
    default void putTemplate(PaymentTemplate template) {
        throw new UnsupportedOperationException("not implemented");
    }

}
