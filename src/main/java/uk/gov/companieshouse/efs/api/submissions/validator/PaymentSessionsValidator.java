package uk.gov.companieshouse.efs.api.submissions.validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
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
            final List<FormTemplate> formTemplateList = formRepository.findByIdFormType(input.getFormDetails().getFormType());
            final Optional<FormTemplate> formTemplate = formTemplateList.stream().findFirst();
            final String formType = formTemplate.map(FormTemplate::getFormType).orElse(null);
            final String formFee = formTemplate.map(FormTemplate::getFee).orElse(null);

            if (StringUtils.isNotBlank(formFee)) {
                final Optional<PaymentTemplate> paymentTemplate =
                    formTemplate.map(FormTemplate::getFee).flatMap(paymentRepository::findById);
                final Optional<PaymentTemplate.Item> firstItem =
                    paymentTemplate.flatMap(p -> p.getItems().stream().findFirst());

                if (paymentTemplate.isPresent()) {
                    if (firstItem.isPresent()) {
                        final BigDecimal decimalAmount = getFeeAmount(input, formType, firstItem.get());

                        checkPaymentSessions(input, formType, hasPaymentSessions, decimalAmount);
                    } else {
                        throw new SubmissionValidationException(String
                            .format("Fee amount is missing for form [%s] in submission [%s]", formType, input.getId()));
                    }
                }
            }
            else {
                checkPaymentSessions(input, formType, hasPaymentSessions, BigDecimal.ZERO);
            }
        }
        super.validate(input);

    }

    private BigDecimal getFeeAmount(final Submission input, final String formType, final PaymentTemplate.Item firstItem)
        throws SubmissionValidationException {
        final String amount = Optional.ofNullable(firstItem).map(PaymentTemplate.Item::getAmount).orElse("");

        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            throw new SubmissionValidationException(String
                .format("Fee amount is missing or invalid for form [%s] in submission [%s]", formType, input.getId()));
        }
    }

    private void checkPaymentSessions(final Submission input, final String formType, final boolean hasPaymentSessions,
        final BigDecimal decimalAmount) throws SubmissionValidationException {
        final Optional<Boolean> requiredFee = Optional.of(decimalAmount).map(d -> d.compareTo(BigDecimal.ZERO) > 0);

        if (requiredFee.filter(Boolean.TRUE::equals).isPresent() && !hasPaymentSessions) {
            throw new SubmissionValidationException(String
                .format("At least one payment session is absent for fee paying form [%s] in submission [%s]", formType,
                    input.getId()));
        } else if (!requiredFee.filter(Boolean.TRUE::equals).isPresent() && hasPaymentSessions) {
            throw new SubmissionValidationException(String
                .format("At least one payment session is present for the non fee paying form [%s] in submission [%s]",
                    formType, input.getId()));
        }
    }

}
