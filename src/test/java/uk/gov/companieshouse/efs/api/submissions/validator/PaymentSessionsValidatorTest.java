package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class PaymentSessionsValidatorTest {
    private static final String TEST_FORM = "FORM";
    private static final String TEST_FEE = "FEE";
    private static final String SUB_ID = "0000000000";

    private PaymentSessionsValidator testValidator;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private PaymentTemplateRepository paymentRepository;
    @Mock
    private Submission submission;
    @Mock
    private FormDetails formDetails;
    @Mock
    private FormTemplate formTemplate;
    @Mock
    private PaymentTemplate paymentTemplate;
    @Mock
    private PaymentTemplate.Item paymentItem;
    @Mock
    private SessionApi paymentSession;
    @Mock
    private Validator<Submission> nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new PaymentSessionsValidator(formRepository, paymentRepository);
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenFormRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new PaymentSessionsValidator(null, paymentRepository);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenPaymentRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new PaymentSessionsValidator(formRepository, null);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFormNotFoundThenValid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        when(formRepository.findById(TEST_FORM)).thenReturn(Optional.empty());

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenPaymentTemplateNotFoundThenInvalid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        when(paymentRepository.findById(TEST_FEE)).thenReturn(Optional.empty());
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Fee amount is missing for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenPaymentItemsEmptyAndHasPaymentSessionsThenInvalid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        when(paymentRepository.findById(TEST_FEE)).thenReturn(Optional.of(paymentTemplate));
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Fee amount is missing for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFeeAmountNullThenInvalid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        when(paymentRepository.findById(TEST_FEE)).thenReturn(Optional.of(paymentTemplate));
        expectPaymentTemplateWithSingleItem();
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Fee amount is missing or invalid for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    private void expectPaymentTemplateWithSingleItem() {
        when(paymentRepository.findById(TEST_FEE)).thenReturn(Optional.of(paymentTemplate));
        when(paymentTemplate.getItems()).thenReturn(Collections.singletonList(paymentItem));
    }

    @Test
    void validateWhenFeeAmountNaNThenInvalid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("N");
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Fee amount is missing or invalid for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFeeAmountZeroThenValid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        when(formRepository.findById(TEST_FORM)).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn(TEST_FEE);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("0.00");

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFeeAmountNonZeroAndNoSessionsThenInvalid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("1");
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("At least one payment session is absent for fee paying form [{0}] in submission [{1}]", TEST_FORM,
                SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFeeAmountNonZeroAndHasPaymentSessionsThenValid() throws SubmissionValidationException {
        final SessionListApi sessions = new SessionListApi(Collections.singletonList(paymentSession));

        when(submission.getPaymentSessions()).thenReturn(sessions);
        expectSubmissionWithForm();
        expectFormDetailsWithFormHasFee(true);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("1");

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFeeAmountZeroAndHasPaymentSessionsThenInvalid() {
        final SessionListApi sessions = new SessionListApi(Collections.singletonList(paymentSession));

        expectSubmissionWithForm();
        expectSubmissionWithPaymentSessions(sessions);
        expectFormDetailsWithFormHasFee(true);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("0.00");
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("At least one payment session is present for the non fee paying form [{0}] in submission [{1}]",
                TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    private void expectSubmissionWithPaymentSessions(SessionListApi sessions) {
        when(submission.getPaymentSessions()).thenReturn(sessions);

    }

    private void expectSubmissionWithForm() {
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn(TEST_FORM);
    }

    private void expectFormDetailsWithFormHasFee(final boolean hasFee) {
        when(formRepository.findById(TEST_FORM)).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn(hasFee ? TEST_FEE : null);
    }

    private void expectErrorMessageDetails() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(formTemplate.getFormType()).thenReturn(TEST_FORM);
    }
}