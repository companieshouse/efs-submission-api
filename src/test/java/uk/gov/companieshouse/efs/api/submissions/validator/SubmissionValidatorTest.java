package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.INSOLVENCY;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

@ExtendWith(MockitoExtension.class)
class SubmissionValidatorTest {

    private static final String SUB_ID = "0000000000";
    private static final String TEST_AMOUNT = "10";
    private static final PaymentTemplate.Item TEST_ITEM =
        PaymentTemplate.Item.newBuilder().withAmount(TEST_AMOUNT).build();
    public static final String FEE_FORM = "SLPCS01";
    public static final String NON_FEE_FORM = "SH01";

    private SubmissionValidator validator;

    @Mock
    private Submission submission;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private CategoryTemplateService categoryTemplateService;
    @Mock
    private PaymentTemplateService paymentTemplateService;

    @Mock
    private FormTemplate formTemplate;
    @Mock
    private CategoryTemplateApi categoryTemplate;
    @Mock
    private PaymentTemplate paymentTemplate;

    @BeforeEach
    void setUp() {
        this.validator = new SubmissionValidator(formRepository, categoryTemplateService, paymentTemplateService);
    }


    @Test
    void testThrowExceptionIfConfirmationReferenceIsNull() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, NON_FEE_FORM, Collections.emptyList()));
        when(submission.getConfirmationReference()).thenReturn(null);

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Confirmation reference is absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testValidateSuccessWithNoPaymentRequired() {
        // given
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, NON_FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn(null);
        when(formTemplate.getFormCategory()).thenReturn("SH");
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi());
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        assertDoesNotThrow(actual);
    }

    @Test
    void testValidateSuccessWithNoPaymentRequiredAndPaymentSessionsPresent() {
        // given
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn(null);
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi(Collections.singletonList(
            new SessionApi("woeirsodiflsj", "E_lLgj6SI8cWoEXVtGMsuB81DoEcOiWPPgSJTz4OQ0gVo0y6d_NDFP7waRQfdU1z"))));
        when(formTemplate.getFormType()).thenReturn(FEE_FORM);
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals(MessageFormat
            .format("At least one payment session is present for the non fee paying form [{0}] in submission [{1}]", FEE_FORM,
                SUB_ID), exception.getMessage());
    }

    @Test
    void testValidateSuccessWithPayment() {
        // given
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn("TEST_FEE");
        when(paymentTemplateService.getTemplate("TEST_FEE")).thenReturn(Optional.of(paymentTemplate));
        when(paymentTemplate.getItems()).thenReturn(Collections.singletonList(TEST_ITEM));
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi(Collections.singletonList(
            new SessionApi("woeirsodiflsj", "E_lLgj6SI8cWoEXVtGMsuB81DoEcOiWPPgSJTz4OQ0gVo0y6d_NDFP7waRQfdU1z"))));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        assertDoesNotThrow(actual);
    }

    @Test
    void testValidateSuccessWithPaymentRequiredAndNoPaymentSessionsPresent() {
        // given
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn("TEST_FEE");
        when(paymentTemplateService.getTemplate("TEST_FEE")).thenReturn(Optional.of(paymentTemplate));
        when(paymentTemplate.getItems()).thenReturn(Collections.singletonList(TEST_ITEM));
        when(formTemplate.getFormType()).thenReturn(FEE_FORM);
        when(submission.getConfirmationReference()).thenReturn("123 456 789");
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi());

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals(MessageFormat
            .format("At least one payment session is absent for fee paying form [{0}] in submission [{1}]", FEE_FORM,
                SUB_ID), exception.getMessage());
    }

    @Test
    void testValidateSuccessWithPaymentTemplateNotFound() {
        // given
        when(submission.getId()).thenReturn(SUB_ID);
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn("TEST_FEE");
        when(paymentTemplateService.getTemplate("TEST_FEE")).thenReturn(Optional.empty());
        when(formTemplate.getFormType()).thenReturn(FEE_FORM);
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals(MessageFormat
                .format("Fee amount is missing or invalid for form [{0}] in submission [{1}]", FEE_FORM, SUB_ID),
            exception.getMessage());
    }

    @Test
    void testThrowExceptionIfPresenterIsNull() {
        //given
        when(submission.getId()).thenReturn("abc");

        //when
        Executable actual = () -> validator.validate(submission);

        //then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Presenter details are absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfPresenterEmailIsAbsent() {
        //given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter(null));

        //when
        Executable actual = () -> validator.validate(submission);

        //then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Presenter email is absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfCompanyIsNull() {
        //given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));

        //when
        Executable actual = () -> validator.validate(submission);

        //then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Company details are absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfCompanyNumberIsAbsent() {
        //given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("", null));

        //when
        Executable actual = () -> validator.validate(submission);

        //then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Company number is absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfCompanyNameIsAbsent() {
        //given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", null));

        //when
        Executable actual = () -> validator.validate(submission);

        //then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Company name is absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfFormIsNull() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Form details are absent in submission [abc]", exception.getMessage());
    }


    @Test
    void testThrowExceptionIfFormTypeIsAbsent() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, null, null));

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Form type is absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfFormTypeIsNotValid() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, "SH99", null));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Form type [SH99] unknown in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfFileDetailsListIsNull() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, NON_FEE_FORM, null));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("File details are absent in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfFileDetailsListIsEmpty() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, NON_FEE_FORM, Collections.emptyList()));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("File details are empty in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfFileDetailsListContainNull() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, NON_FEE_FORM, Collections.singletonList(null)));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("File details contains null in submission [abc]", exception.getMessage());
    }

    @Test
    void testThrowExceptionIfSubmissionForFesEnabledFormContainsMultipleFiles() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails()).thenReturn(new FormDetails(null, NON_FEE_FORM,
            Arrays.asList(FileDetails.builder().build(), FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.getFee()).thenReturn(null);
        when(formTemplate.getFormType()).thenReturn(NON_FEE_FORM);
        when(formTemplate.isFesEnabled()).thenReturn(true);
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Attachments present in submission [abc] for FES enabled form [SH01]",
                exception.getMessage());
    }

    @Test
    void testIfSubmissionForFesEnabledFormContainsOneFile() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, NON_FEE_FORM, Arrays.asList(FileDetails.builder().build())));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(formTemplate.isFesEnabled()).thenReturn(true);
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        assertDoesNotThrow(actual);
    }

    @Test
    void testThrowExceptionIfConfirmAuthorisedRequired() {
        // given
        when(submission.getId()).thenReturn("abc");
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(formTemplate));
        when(submission.getFormDetails()).thenReturn(
            new FormDetails(null, "AM01", Arrays.asList(FileDetails.builder().build(), FileDetails.builder().build())));
        when(categoryTemplateService.getTopLevelCategory(anyString())).thenReturn(INSOLVENCY);
        when(formTemplate.getFormCategory()).thenReturn("ADMIN");
        when(formTemplate.getFormType()).thenReturn("AM01");
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // when
        Executable actual = () -> validator.validate(submission);

        // then
        SubmissionValidationException exception = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("Presenter must confirm they are authorised in submission [abc] for Insolvency form [AM01]",
            exception.getMessage());
    }
}
