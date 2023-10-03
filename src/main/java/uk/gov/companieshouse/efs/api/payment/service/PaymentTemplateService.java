package uk.gov.companieshouse.efs.api.payment.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;

/**
 * Stores and retrieves the payment template information
 */
public interface PaymentTemplateService {

    /**
     * Retrieve the template for the specified id active at the specified date/time
     *
     * @param fee the payment template id of the template
     * @param activeAt the local date/time for which the template must be active
     * @return the payment template
     */
    default Optional<PaymentTemplate> getPaymentTemplate(String fee, LocalDateTime activeAt) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve the all templates for the specified id
     *
     * @param fee the payment template id of the template(s)
     * @return the payment template
     */
    default List<PaymentTemplate> getPaymentTemplates(String fee) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve the all templates
     *
     * @return the payment template list
     */
    default List<PaymentTemplate> getPaymentTemplates() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Store the payment template
     *
     * @param template the payment template to be stored
     * @return
     */
    default PaymentTemplate postTemplate(PaymentTemplate template) {
        throw new UnsupportedOperationException("not implemented");
    }

}
