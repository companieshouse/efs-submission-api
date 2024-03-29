package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
    private static final LocalDateTime NOW = LocalDateTime.now();

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

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        testValidator = new PaymentSessionsValidator(formRepository, paymentRepository, clock);
        testValidator.setNext(nextValidator);
    }

    @Test
    void validateWhenFormRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new PaymentSessionsValidator(null, paymentRepository, clock);
        testValidator.setNext(nextValidator);

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenPaymentRepositoryNullThenValid() throws SubmissionValidationException {
        testValidator = new PaymentSessionsValidator(formRepository, null, clock);
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
    void validateWhenPaymentItemsEmptyAndHasPaymentSessionsThenInvalid() {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
        when(
            paymentRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEST_FEE, LocalDateTime.now(clock))).thenReturn(Optional.of(paymentTemplate));
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(),
            is(MessageFormat.format("Fee item is missing for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFormFeeBlankAndHasPaymentSessionsThenInvalid() {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate("");
        expectSubmissionWithPaymentSessions(new SessionListApi(Collections.singletonList(paymentSession)));
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("At least one payment session is present for the non fee paying form [{0}] in submission [{1}]",
                TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFormFeeNotFoundThenValid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
        when(
            paymentRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEST_FEE, LocalDateTime.now(clock))).thenReturn(Optional.empty());

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenPaymentTemplateNotFoundThenValid() throws SubmissionValidationException {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
        when(
            paymentRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEST_FEE, LocalDateTime.now(clock))).thenReturn(Optional.empty());

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFeeAmountNullThenInvalid() {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
        expectPaymentTemplateWithSingleItem();
        expectErrorMessageDetails();

        final SubmissionValidationException exception =
            assertThrows(SubmissionValidationException.class, () -> testValidator.validate(submission));

        assertThat(exception.getMessage(), is(MessageFormat
            .format("Fee amount is missing or invalid for form [{0}] in submission [{1}]", TEST_FORM, SUB_ID)));
        verifyNoInteractions(nextValidator);
    }

    @Test
    void validateWhenFeeAmountNaNThenInvalid() {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
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
    void validateWhenFeeAmountNonZeroAndNoPaymentSessionsThenInvalid() {
        expectSubmissionWithForm();
        expectFormWithPaymentTemplate(TEST_FEE);
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
        expectFormWithPaymentTemplate(TEST_FEE);
        expectPaymentTemplateWithSingleItem();
        when(paymentItem.getAmount()).thenReturn("1");

        testValidator.validate(submission);

        verify(nextValidator).validate(submission);
    }

    @Test
    void validateWhenFeeAmountZeroAndHasPaymentSessionsThenInvalid() {
        expectSubmissionWithForm();
        expectSubmissionWithPaymentSessions(new SessionListApi(Collections.singletonList(paymentSession)));
        expectFormWithPaymentTemplate(TEST_FEE);
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

    private void expectFormWithPaymentTemplate(final String paymentTemplateId) {
        when(formRepository.findById(TEST_FORM)).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFormType()).thenReturn(TEST_FORM);
        when(formTemplate.getFee()).thenReturn(paymentTemplateId);
    }

    private void expectPaymentTemplateWithSingleItem() {
        when(
            paymentRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEST_FEE, LocalDateTime.now(clock))).thenReturn(Optional.of(paymentTemplate));
        when(paymentTemplate.getItems()).thenReturn(Collections.singletonList(paymentItem));
    }

    private void expectErrorMessageDetails() {
        when(submission.getId()).thenReturn(SUB_ID);
        when(formTemplate.getFormType()).thenReturn(TEST_FORM);
    }
}