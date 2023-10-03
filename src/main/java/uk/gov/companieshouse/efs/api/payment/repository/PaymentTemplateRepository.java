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
    @SuppressWarnings("java:S100")
    Optional<PaymentTemplate> findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
        String fee, LocalDateTime activeAt);

    @SuppressWarnings("java:S100")
    List<PaymentTemplate> findById_FeeOrderById_ActiveFromDesc(String fee);


}