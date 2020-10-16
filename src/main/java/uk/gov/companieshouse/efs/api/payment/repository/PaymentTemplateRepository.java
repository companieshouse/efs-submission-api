package uk.gov.companieshouse.efs.api.payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;

/**
 * Store and retrieve payment template information
 */
public interface PaymentTemplateRepository extends MongoRepository<PaymentTemplate, String> {

}
