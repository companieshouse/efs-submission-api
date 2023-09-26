package uk.gov.companieshouse.efs.api.payment.service;

import java.time.LocalDateTime;
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

    /**
     * PaymentTemplateService constructor
     *
     * @param repository the {@link PaymentTemplateRepository}
     */
    @Autowired
    public PaymentTemplateServiceImpl(final PaymentTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentTemplate> getTemplate(final String fee, final LocalDateTime activeAt) {
        return repository.findFirstById_FeeAndId_StartTimestampLessThanEqualOrderById_StartTimestampDesc(fee,
            activeAt);

    }

    @Override
    public void putTemplate(final PaymentTemplate template) {
        repository.save(template);
    }

}
