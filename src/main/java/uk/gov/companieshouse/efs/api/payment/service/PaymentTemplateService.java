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
     * Find the single {@link PaymentTemplate} having {@code fee} equal to the specified {@code fee}
     * and {@code activeFrom} on or before the specified {@code activeAt} date/time.
     *
     * @param fee      the fee ID to query
     * @param activeAt the local date/time to query
     * @return the {@link PaymentTemplate} found, if any, wrapped in an {@link Optional}.
     */
    default Optional<PaymentTemplate> getPaymentTemplate(String fee, LocalDateTime activeAt) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Find the {@link PaymentTemplate}s having {@code fee} equal to the specified {@code fee}.
     *
     * @param fee the fee ID to query
     * @return the collection of {@link PaymentTemplate} found, if any, wrapped in a {@link List}.
     */
    default List<PaymentTemplate> getPaymentTemplates(String fee) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Find all {@link PaymentTemplate}s.
     *
     * @return the collection of {@link PaymentTemplate} found, if any, wrapped in a {@link List}.
     */
    default List<PaymentTemplate> getPaymentTemplates() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Store the {@link PaymentTemplate}.
     *
     * @param template the {@link PaymentTemplate} to be stored
     * @return the {@link PaymentTemplate} stored
     */
    default PaymentTemplate postTemplate(PaymentTemplate template) {
        throw new UnsupportedOperationException("not implemented");
    }

}
