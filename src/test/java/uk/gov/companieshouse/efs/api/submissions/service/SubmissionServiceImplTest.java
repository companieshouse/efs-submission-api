package uk.gov.companieshouse.efs.api.submissions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.CompanyMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.FileDetailsMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.PresenterMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionMapper;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.Validator;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {
    public static final String SESSION_ID = "2222222222";
    public static final String SESSION_STATE =
        "FD_RlzcLp-xcK1YZGEbn3ZpRHGlwy7tNjn_zsjYVauoB8Ml3GkfpmbhPuPd093XM";
    private static final String SUBMISSION_ID = "123";
    public static final String STATUS_PAID = PaymentClose.Status.PAID.toString();
    private static final String STATUS_FAILED = PaymentClose.Status.FAILED.toString();
    private static final String STATUS_CANCELLED = PaymentClose.Status.CANCELLED.toString();
    public static final String EXPECTED_UPDATE_ERROR_MSG =
        "Submission status for [123] wasn't in [OPEN, PAYMENT_REQUIRED, "
            + "PAYMENT_FAILED], couldn't update";

    public static final String EXPECTED_COMPLETE_ERROR_MSG =
        "Submission status for [123] wasn't in [OPEN, PAYMENT_REQUIRED, "
            + "PAYMENT_FAILED, SUBMITTED], couldn't update";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private SubmissionService submissionService;

    @Captor
    private ArgumentCaptor<Submission> submissionCaptor;

    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private PresenterMapper presenterMapper;
    @Mock
    private FileDetailsMapper fileDetailsMapper;
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private CurrentTimestampGenerator timestampGenerator;
    @Mock
    private ConfirmationReferenceGeneratorService confirmationReferenceGenerator;
    @Mock
    private Submission submission;
    @Mock
    private FormDetails formDetails;
    @Mock
    private FileDetails fileDetails;
    @Mock
    private SubmissionApi submissionApi;
    @Mock
    private Validator<Submission> validator;
    @Mock
    private FormTemplateService formTemplateService;
    @Mock
    private PaymentTemplateService paymentTemplateService;
    @Mock
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        submissionService =
            new SubmissionServiceImpl(submissionRepository, submissionMapper, presenterMapper,
                companyMapper, fileDetailsMapper, timestampGenerator,
                confirmationReferenceGenerator, formTemplateService, paymentTemplateService,
                emailService, validator);
    }

    @Test
    void testCreateSubmission() {
        // given
        PresenterApi presenterApi = mock(PresenterApi.class);
        Presenter presenter = mock(Presenter.class);
        when(presenterMapper.map(presenterApi)).thenReturn(presenter);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        submissionService.createSubmission(presenterApi);

        // then
        verify(presenterMapper).map(presenterApi);
        verify(submissionRepository).create(any(Submission.class));
        verify(timestampGenerator).generateTimestamp();
        verify(confirmationReferenceGenerator).generateId();
    }

    @Test
    void testUpdateSubmissionWithCompany() {
        // given
        CompanyApi companyApi = mock(CompanyApi.class);
        Company company = mock(Company.class);
        when(companyMapper.map(companyApi)).thenReturn(company);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(companyMapper).map(companyApi);
        verify(submissionRepository).updateSubmission(argThat(s-> s.getCompany().equals(company)));
    }

    @Test
    void testUpdateSubmissionWithCompanyNotFound() {
        // given
        CompanyApi companyApi = mock(CompanyApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(companyMapper);
    }


    @Test
    void testUpdateSubmissionWithCompanyIncorrectState() {
        // given
        CompanyApi companyApi = mock(CompanyApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
        verifyNoInteractions(companyMapper);
    }

    @Test
    void testUpdateSubmissionWithForm() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getFeeOnSubmission(), is(equalTo(submission.getFeeOnSubmission())));
        assertThat(updateSubmission.getFormDetails().getFormType(), is(equalTo(formApi.getFormType())));
    }

    @Test
    void testUpdateSubmissionWithFormWhenFormTypeNull() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);

        when(formApi.getFormType()).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission expected = submissionCaptor.getValue();
        assertThat(expected.getFeeOnSubmission(), is(nullValue()));
        assertThat(expected.getFormDetails().getFormType(), is(nullValue()));
        verifyNoInteractions(formTemplateService, paymentTemplateService);
    }

    private static Stream<FormTypeApi> provideScenariosForUpdateSubmissionWithForm() {
        return Stream.of(new FormTypeApi(), new FormTypeApi("abc"));
    }
    @ParameterizedTest
    @MethodSource("provideScenariosForUpdateSubmissionWithForm")
    void testUpdateSubmissionWithFormScenarios(FormTypeApi formType) {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn(formType.getFormType());

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formType);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission expected = submissionCaptor.getValue();
        if (formType.getFormType()==null) {
            assertThat(expected.getFeeOnSubmission(), is(nullValue()));
            assertThat(expected.getFormDetails().getFormType(), is(nullValue()));
        }
        else{
            assertThat(expected.getFeeOnSubmission(), is(equalTo(submission.getFeeOnSubmission())));
            assertThat(expected.getFormDetails().getFormType(), is(equalTo(formType.getFormType())));
            verify(formTemplateService).getFormTemplate(formType.getFormType());
        }
        verifyNoInteractions(paymentTemplateService);
        verifyNoMoreInteractions(formTemplateService, paymentTemplateService);
    }

    @Test
    void  testUpdateSubmissionWithFormWhenFormDetailsNull() {
        FormTypeApi formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(null);
        when(formApi.getFormType()).thenReturn("abc");

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFormDetails().getFormType().equals("abc")));
        verify(formTemplateService).getFormTemplate("abc");
    }


    @Test
    void testUpdateSubmissionWithFormWhenFormTypeNotFound() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        final String FORM_TYPE = "NOT_FOUND";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(formTemplateService).getFormTemplate(FORM_TYPE);
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
        verifyNoInteractions(paymentTemplateService);
    }


    @Test
    void testUpdateSubmissionWithFormWhenPaymentChargeNull() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        final String FORM_TYPE = "NULL_CHARGE";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
        verifyNoInteractions(paymentTemplateService);
    }

    @Test
    void testUpdateSubmissionWithFormWhenPaymentTemplateNotFound() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        final String FORM_TYPE = "NULL_CHARGE";
        final String PAYMENT_TEMPLATE = "PAYMENT";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(PAYMENT_TEMPLATE);
        when(paymentTemplateService.getTemplate(PAYMENT_TEMPLATE)).thenReturn(Optional.empty());
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
    }

    @Test
    void testUpdateSubmissionWithFormWhenPaymentTemplateFound() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        final String FORM_TYPE = "NULL_CHARGE";
        final String PAYMENT_TEMPLATE = "PAYMENT";
        final String PAYMENT_CHARGE = "99";
        PaymentTemplate template =
            PaymentTemplate.newBuilder().withItem(PaymentTemplate.Item.newBuilder().withAmount(PAYMENT_CHARGE).build()).build();

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(PAYMENT_TEMPLATE);
        when(paymentTemplateService.getTemplate(PAYMENT_TEMPLATE)).thenReturn(Optional.of(template));
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> PAYMENT_CHARGE.equals(s.getFeeOnSubmission())));
    }

    @Test
    void testUpdateSubmissionWithFormWhereFormDetailsAlreadyExist() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().equals(formDetails)));
    }

    @Test
    void testUpdateSubmissionWithFormNotFound() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithFormIncorrectState() {
        // given
        FormTypeApi formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithFiles() {
        // given
        FileListApi fileListApi = mock(FileListApi.class);
        List<FileDetails> fileDetailsList = Collections.singletonList(mock(FileDetails.class));
        when(fileDetailsMapper.map(fileListApi)).thenReturn(fileDetailsList);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(fileDetailsMapper).map(fileListApi);
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().getFileDetailsList().equals(fileDetailsList)));
    }

    @Test
    void testUpdateSubmissionWithFilesWhereFormDetailsAlreadyExist() {
        // given
        FileListApi fileListApi = mock(FileListApi.class);
        List<FileDetails> fileDetailsList = Collections.singletonList(mock(FileDetails.class));
        when(fileDetailsMapper.map(fileListApi)).thenReturn(fileDetailsList);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(fileDetailsMapper).map(fileListApi);
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().equals(formDetails)));
    }

    @Test
    void testUpdateSubmissionWithFilesNotFound() {
        // given
        FileListApi fileListApi = mock(FileListApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    void testUpdateSubmissionWithFilesIncorrectState() {
        // given
        FileListApi fileListApi = mock(FileListApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenOneExists() {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission expected = submissionCaptor.getValue();
        assertThat(expected.getPaymentSessions(), is(sessionListApi));
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenNoneExist() {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PAID.toString());
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission expected = submissionCaptor.getValue();
        assertThat(expected.getPaymentSessions(), is(sessionListApi));
        assertThat(expected.getStatus(), is(SubmissionStatus.PAYMENT_REQUIRED));
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenPaymentReferenceNotFound() {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual =
            () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID,
                sessionListApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenIncorrectState() {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual =
            () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID,
                sessionListApi);

        // then
        SubmissionIncorrectStateException ex =
            assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
    }

    @Test
    void testCompleteSubmissionWhenNoFee() throws SubmissionValidationException {
        // given
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN).thenReturn(SubmissionStatus.SUBMITTED);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW.minusSeconds(1L))
            .thenReturn(NOW);
        // when
        SubmissionResponseApi actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(timestampGenerator, times(2)).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission expected = submissionCaptor.getValue();
        assertThat(expected.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(expected.getSubmittedAt(), is(NOW.minusSeconds(1L)));
        assertThat(expected.getLastModifiedAt(), is(NOW));

        verify(validator).validate(submission);
        verify(emailService).sendExternalConfirmation(
            new ExternalNotificationEmailModel(submission));
    }

    @Test
    void testCompleteSubmissionWhenFeeBeforePaymentPatch() throws SubmissionValidationException {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        when(submission.getFeeOnSubmission()).thenReturn("1");
        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        // when
        SubmissionResponseApi actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoInteractions(timestampGenerator);
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionWhenNoFeeAndNotSubmitted() throws SubmissionValidationException {
        // given
        // FAILED session here for test coverage purposes only
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.FAILED.toString());

        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        SubmissionIncorrectStateException exception =
            assertThrows(SubmissionIncorrectStateException.class, () -> submissionService.completeSubmission(SUBMISSION_ID));
        assertEquals("Submission status for [123] wasn't in [SUBMITTED], couldn't update", exception.getMessage());

        // then
        verify(timestampGenerator, times(2)).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionWhenPaidAfterPaymentPatch() throws SubmissionValidationException {
        // given
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PAID.toString());

        when(submission.getFeeOnSubmission()).thenReturn("1");
        when(submission.getStatus()).thenReturn(SubmissionStatus.SUBMITTED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(validator).validate(submission);
        verifyNoInteractions(timestampGenerator);
    }

    @Test
    void testCompleteSubmissionWhenFailedAfterPaymentPatch() throws SubmissionValidationException {
        // given
        when(submission.getFeeOnSubmission()).thenReturn("1");
        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_FAILED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoMoreInteractions(submissionRepository);
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionValidationFailure() throws SubmissionValidationException {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        doThrow(new SubmissionValidationException("bad data")).when(validator).validate(submission);

        // when
        Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        SubmissionValidationException ex = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("bad data", ex.getMessage());
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionNotFound() {
        // given
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(validator);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionIncorrectState() {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.REJECTED_BY_VIRUS_SCAN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_COMPLETE_ERROR_MSG, ex.getMessage());
        verifyNoInteractions(validator);
        verifyNoInteractions(emailService);
    }

    @Test
    void testUpdateSubmissionQueued() {
        // given
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionQueued(submission);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.PROCESSING));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));

    }

    @Test
    void testReadSubmission() {
        // given
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submissionMapper.map(submission)).thenReturn(submissionApi);
        // when
        SubmissionApi actual = submissionService.readSubmission(SUBMISSION_ID);
        // then
        assertEquals(submissionApi, actual);
        verify(submissionRepository).read(SUBMISSION_ID);
        verify(submissionMapper).map(submission);
    }

    @Test
    void testReadSubmissionDoesNotMapMissingSubmission() {
        // when
        SubmissionApi actual = submissionService.readSubmission(SUBMISSION_ID);

        // then
        assertNull(actual);
        verify(submissionRepository).read(SUBMISSION_ID);
        verifyNoInteractions(submissionMapper);
    }

    @Test
    void testUpdateSubmissionBarcode() {
        // given
        String barcode = "Y1234ABCD";
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionBarcode(SUBMISSION_ID, barcode);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateBarcode(SUBMISSION_ID, barcode);
    }

    @Test
    void testUpdateSubmissionStatus() {
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionStatus(SUBMISSION_ID,
                SubmissionStatus.ACCEPTED);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmissionStatus(SUBMISSION_ID, SubmissionStatus.ACCEPTED);
    }

    @Test
    void testUpdateSubmission() {
        // given
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);
        
        // when
        submissionService.updateSubmission(submission);

        // then
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(argThat(s->s.getLastModifiedAt().equals(NOW)));
    }

    @Test
    void testUpdateSubmissionConfirmAuthorised() {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionConfirmAuthorised(SUBMISSION_ID, true);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s->s.getConfirmAuthorised().equals(Boolean.TRUE)));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenNotFound() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);

        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(null);

        // when
        final SubmissionNotFoundException exception =
            assertThrows(SubmissionNotFoundException.class,
                () -> submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID,
                    paymentClose));

        // then
        assertThat(exception.getMessage(), is("Could not locate submission with id: [123]"));
        verifyNoMoreInteractions(submission);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenIncorrectStatus() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);
        final SessionApi paySession =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_FAILED, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);

        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(),
            is(STATUS_FAILED));
        verifyNoMoreInteractions(submission, emailService);

    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaidBeforeCompletion() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.PAID);
        final SessionApi paySession =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(),
            is(STATUS_PAID));
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
        verifyNoMoreInteractions(emailService);

    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenFailedBeforeCompletion() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);
        final SessionApi paySession =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.OPEN, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);

        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(),
            is(STATUS_FAILED));
        verifyNoMoreInteractions(submission, emailService);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentSessionNotMatched() {
        // given
        final PaymentClose paymentClose =
            new PaymentClose(SESSION_ID + "X", PaymentClose.Status.FAILED);
        final SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.OPEN, sessionApi);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);

        // when
        final SubmissionIncorrectStateException exception =
            assertThrows(SubmissionIncorrectStateException.class,
                () -> submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID,
                    paymentClose));

        assertThat(exception.getMessage(), is("payment reference not matched"));
        verifyNoInteractions(emailService);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndPaid() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.PAID);
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.SUBMITTED);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(), is(STATUS_PAID));
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        Submission updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getSubmittedAt(), is(NOW));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndFailed() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);


        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(), is(STATUS_FAILED));
        verify(submissionRepository).read(SUBMISSION_ID);
        verify(submissionRepository).updateSubmission(argThat(s->s.getStatus() == SubmissionStatus.PAYMENT_FAILED));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndCancelled() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.CANCELLED);
        SessionApi sessionApi =
                new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final SubmissionResponseApi actual =
                submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(), is(STATUS_CANCELLED));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verify(submissionRepository).read(SUBMISSION_ID);
        verifyNoMoreInteractions(submissionRepository);
        verifyNoInteractions(timestampGenerator);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentAlreadyFailed() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);
        SessionApi sessionApi =
                new SessionApi(SESSION_ID, SESSION_STATE, PaymentClose.Status.FAILED.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_FAILED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final SubmissionResponseApi actual =
                submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().get(0).getSessionStatus(), is(STATUS_FAILED));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoMoreInteractions(submissionRepository);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentError() {
        // given
        final PaymentClose paymentClose = new PaymentClose(SESSION_ID, PaymentClose.Status.ERROR);
        SessionApi sessionApi =
            new SessionApi(SESSION_ID, SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final SubmissionResponseApi actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoMoreInteractions(submissionRepository);
    }

    private void expectSubmissionWithPaymentSession(final SubmissionStatus submissionStatus,
        final SessionApi sessionApi) {
        final SessionListApi sessionListApi =
            new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(submissionStatus);
        when(submission.getPaymentSessions()).thenReturn(sessionListApi);
    }
}