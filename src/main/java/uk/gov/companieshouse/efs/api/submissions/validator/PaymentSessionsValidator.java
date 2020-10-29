package uk.gov.companieshouse.efs.api.submissions.validator;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.util.CollectionUtils;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class PaymentSessionsValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    private final FormTemplateRepository formRepository;
    private final PaymentTemplateRepository paymentRepository;

    public PaymentSessionsValidator(final FormTemplateRepository formTemplateRepository,
        final PaymentTemplateRepository paymentTemplateRepository) {
        this.formRepository = formTemplateRepository;
        this.paymentRepository = paymentTemplateRepository;
    }

    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        final boolean hasPaymentSessions = !CollectionUtils.isEmpty(input.getPaymentSessions());

        if (formRepository != null && paymentRepository != null) {
            final Optional<FormTemplate> formTemplate = formRepository.findById(input.getFormDetails().getFormType());
            final Optional<PaymentTemplate> paymentTemplate = formTemplate.flatMap(this::mapToPaymentTemplate);
            final Optional<PaymentTemplate.Item> firstItem =
                paymentTemplate.flatMap(p -> p.getItems().stream().findFirst());
            final Optional<String> amount = firstItem.map(PaymentTemplate.Item::getAmount);

            if (amount.isPresent()) {
                final Optional<BigDecimal> decimalAmount = amount.
                    flatMap(this::getDecimalAmount);
                final boolean requiredFee = decimalAmount.filter(d -> d.compareTo(BigDecimal.ZERO) > 0).isPresent();

                if (requiredFee && !hasPaymentSessions) {
                    throw new SubmissionValidationException(String.format("At least one payment session is absent for fee paying form [%s] in submission [%s]",
                        formTemplate.map(FormTemplate::getFormType).orElse(null), input.getId()));
                } else if (!requiredFee && hasPaymentSessions) {
                    throw new SubmissionValidationException(String.format(
                        "At least one payment session is present for the non fee paying form [%s] in submission [%s]",
                        formTemplate.map(FormTemplate::getFormType).orElse(null), input.getId()));
                }
            }
        }
        super.validate(input);
    }

    private Optional<PaymentTemplate> mapToPaymentTemplate(final FormTemplate formTemplate) {
        return Optional.of(formTemplate).flatMap(f -> paymentRepository.findById(f.getFee()));
    }

    private Optional<BigDecimal> getDecimalAmount(final String amount) {
        try {
            final BigDecimal feeAmount = new BigDecimal(amount);

            return Optional.of(feeAmount);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

    }

}
