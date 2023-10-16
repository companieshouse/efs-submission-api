package uk.gov.companieshouse.efs.api.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;

/**
 * Store and retrieve payment template information
 */
public interface PaymentTemplateRepository extends MongoRepository<PaymentTemplate, String> {
    /**
     * Find the single {@link PaymentTemplate} having {@code fee} equal to the specified {@code fee}
     * and {@code activeFrom} on or before the specified {@code activeAt} date/time.
     *
     * @param fee      the fee ID to query
     * @param activeAt the local date/time to query
     * @return the {@link PaymentTemplate} found, if any, wrapped in an {@link Optional}.
     */
    @SuppressWarnings("java:S100")
    Optional<PaymentTemplate> findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
        String fee, LocalDateTime activeAt);

    /**
     * Find list of {@link PaymentTemplate}s having {@code fee} equal to the specified {@code fee}.
     *
     * @param fee the fee ID to query
     * @return the collection of {@link PaymentTemplate} found, if any, wrapped in a {@link List}.
     */
    @SuppressWarnings("java:S100")
    List<PaymentTemplate> findById_FeeOrderById_ActiveFromDesc(String fee);

}