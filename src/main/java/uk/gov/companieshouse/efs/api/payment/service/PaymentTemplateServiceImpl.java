package uk.gov.companieshouse.efs.api.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.config.Config;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;

/**
 * Stores and retrieves the payment template information
 */
@Service
@Import(Config.class)
public class PaymentTemplateServiceImpl implements PaymentTemplateService {
    private final PaymentTemplateRepository repository;

    @Autowired
    public PaymentTemplateServiceImpl(final PaymentTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentTemplate> getPaymentTemplate(final String fee, final LocalDateTime activeAt) {
        return repository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
            fee, activeAt);

    }

    @Override
    public List<PaymentTemplate> getPaymentTemplates(String fee) {
        return repository.findById_FeeOrderById_ActiveFromDesc(fee);
    }

    @Override
    public List<PaymentTemplate> getPaymentTemplates() {
        return repository.findAll();
    }

    @Override
    public PaymentTemplate postTemplate(final PaymentTemplate template) {
        return repository.save(template);
    }

}
