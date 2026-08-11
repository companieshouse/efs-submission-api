package uk.gov.companieshouse.efs.api.submissions.service;

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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
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

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {
    public static final String PAY_SESSION_ID = "2222222222";
    public static final String PAY_SESSION_STATE =
        "FD_RlzcLp-xcK1YZGEbn3ZpRHGlwy7tNjn_zsjYVauoB8Ml3GkfpmbhPuPd093XM";
    private static final String SUBMISSION_ID = "submission-id";
    public static final String STATUS_PAID = PaymentClose.Status.PAID.toString();
    private static final String STATUS_FAILED = PaymentClose.Status.FAILED.toString();
    private static final String STATUS_CANCELLED = PaymentClose.Status.CANCELLED.toString();
    public static final String EXPECTED_UPDATE_ERROR_MSG =
        "Submission status for [%s] wasn't in [OPEN, PAYMENT_REQUIRED, PAYMENT_FAILED], couldn't update".formatted(SUBMISSION_ID);

    public static final String EXPECTED_COMPLETE_ERROR_MSG =
        "Submission status for [%s] wasn't in [OPEN, PAYMENT_REQUIRED, PAYMENT_FAILED, SUBMITTED], couldn't update".formatted(SUBMISSION_ID);
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

    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        submissionService =
            new SubmissionServiceImpl(submissionRepository, submissionMapper, presenterMapper,
                companyMapper, fileDetailsMapper, timestampGenerator,
                confirmationReferenceGenerator, formTemplateService, paymentTemplateService,
                emailService, validator, clock);
    }

    @Test
    void testCreateSubmission() {
        // given
        final var presenterApi = mock(PresenterApi.class);
        final var presenter = mock(Presenter.class);
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
        final var companyApi = mock(CompanyApi.class);
        final var company = mock(Company.class);
        when(companyMapper.map(companyApi)).thenReturn(company);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(companyMapper).map(companyApi);
        verify(submissionRepository).updateSubmission(argThat(s-> s.getCompany().equals(company)));
    }

    @Test
    void testUpdateSubmissionWithCompanyNotFound() {
        // given
        final var companyApi = mock(CompanyApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        final var ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID), ex.getMessage());
        verifyNoInteractions(companyMapper);
    }


    @Test
    void testUpdateSubmissionWithCompanyIncorrectState() {
        // given
        final var companyApi = mock(CompanyApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        final var ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
        verifyNoInteractions(companyMapper);
    }

    @Test
    void testUpdateSubmissionWithForm() {
        // given
        final var formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getFeeOnSubmission(), is(equalTo(submission.getFeeOnSubmission())));
        assertThat(updateSubmission.getFormDetails().getFormType(), is(equalTo(formApi.getFormType())));
    }

    @Test
    void testUpdateSubmissionWithFormWhenFormTypeNull() {
        // given
        final var formApi = mock(FormTypeApi.class);

        when(formApi.getFormType()).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var expected = submissionCaptor.getValue();
        assertThat(expected.getFeeOnSubmission(), is(nullValue()));
        assertThat(expected.getFormDetails().getFormType(), is(nullValue()));
        verifyNoInteractions(formTemplateService, paymentTemplateService);
    }

    private static Stream<FormTypeApi> provideScenariosForUpdateSubmissionWithForm() {
        return Stream.of(new FormTypeApi(), new FormTypeApi("abc"));
    }
    @ParameterizedTest
    @MethodSource("provideScenariosForUpdateSubmissionWithForm")
    void testUpdateSubmissionWithFormScenarios(final FormTypeApi formType) {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn(formType.getFormType());

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formType);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var expected = submissionCaptor.getValue();
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
        final var formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(null);
        when(formApi.getFormType()).thenReturn("abc");

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFormDetails().getFormType().equals("abc")));
        verify(formTemplateService).getFormTemplate("abc");
    }


    @Test
    void testUpdateSubmissionWithFormWhenFormTypeNotFound() {
        // given
        final var formApi = mock(FormTypeApi.class);
        final var FORM_TYPE = "NOT_FOUND";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(formTemplateService).getFormTemplate(FORM_TYPE);
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
        verifyNoInteractions(paymentTemplateService);
    }


    @Test
    void testUpdateSubmissionWithFormWhenPaymentChargeNull() {
        // given
        final var formApi = mock(FormTypeApi.class);
        final var formTemplateApi = mock(FormTemplateApi.class);
        final var FORM_TYPE = "NULL_CHARGE";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(null);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
        verifyNoInteractions(paymentTemplateService);
    }

    @Test
    void testUpdateSubmissionWithFormWhenPaymentTemplateNotFound() {
        // given
        final var formApi = mock(FormTypeApi.class);
        final var formTemplateApi = mock(FormTemplateApi.class);
        final var FORM_TYPE = "NULL_CHARGE";
        final var PAYMENT_TEMPLATE = "PAYMENT";

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(PAYMENT_TEMPLATE);
        when(paymentTemplateService.getPaymentTemplate(PAYMENT_TEMPLATE, NOW)).thenReturn(Optional.empty());
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> s.getFeeOnSubmission() == null));
    }

    @Test
    void testUpdateSubmissionWithFormWhenPaymentTemplateFound() {
        // given
        final var formApi = mock(FormTypeApi.class);
        final var formTemplateApi = mock(FormTemplateApi.class);
        final var FORM_TYPE = "NULL_CHARGE";
        final var PAYMENT_TEMPLATE = "PAYMENT";
        final var PAYMENT_CHARGE = "99";
        final var template =
            PaymentTemplate.newBuilder()
                .withItem(PaymentTemplate.Item.newBuilder().withAmount(PAYMENT_CHARGE).build())
                .build();

        when(formApi.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateApi);
        when(formTemplateApi.getPaymentCharge()).thenReturn(PAYMENT_TEMPLATE);
        when(paymentTemplateService.getPaymentTemplate(PAYMENT_TEMPLATE, NOW)).thenReturn(Optional.of(template));
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s-> PAYMENT_CHARGE.equals(s.getFeeOnSubmission())));
    }

    @Test
    void testUpdateSubmissionWithFormWhereFormDetailsAlreadyExist() {
        // given
        final var formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().equals(formDetails)));
    }

    @Test
    void testUpdateSubmissionWithFormNotFound() {
        // given
        final var formApi = mock(FormTypeApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        final var ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID), ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithFormIncorrectState() {
        // given
        final var formApi = mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        final var ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithFiles() {
        // given
        final var fileListApi = mock(FileListApi.class);
        final var fileDetailsList = Collections.singletonList(mock(FileDetails.class));
        when(fileDetailsMapper.map(fileListApi)).thenReturn(fileDetailsList);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(fileDetailsMapper).map(fileListApi);
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().getFileDetailsList().equals(fileDetailsList)));
    }

    @Test
    void testUpdateSubmissionWithFilesWhereFormDetailsAlreadyExist() {
        // given
        final var fileListApi = mock(FileListApi.class);
        final var fileDetailsList = Collections.singletonList(mock(FileDetails.class));
        when(fileDetailsMapper.map(fileListApi)).thenReturn(fileDetailsList);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(fileDetailsMapper).map(fileListApi);
        verify(submissionRepository).updateSubmission(argThat(s->s.getFormDetails().equals(formDetails)));
    }

    @Test
    void testUpdateSubmissionWithFilesNotFound() {
        // given
        final var fileListApi = mock(FileListApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        final var ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID), ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    void testUpdateSubmissionWithFilesIncorrectState() {
        // given
        final var fileListApi = mock(FileListApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final Executable actual = () -> submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        final var ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(EXPECTED_UPDATE_ERROR_MSG, ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    void shouldClearSubmissionFilesWhenFormDetailsExist() {
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(formDetails);

        final var actual = submissionService.clearSubmissionFiles(SUBMISSION_ID);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(formDetails).setFileDetailsList(null);
        verify(submissionRepository).updateSubmission(argThat(s -> s.getFormDetails().equals(formDetails)));
    }

    @Test
    void shouldClearSubmissionFilesWhenFormDetailsDoNotExist() {
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(null);

        final var actual = submissionService.clearSubmissionFiles(SUBMISSION_ID);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository).updateSubmission(argThat(s -> s.getFormDetails() == null));
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenOneExists() {
        // given
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        final var sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var expected = submissionCaptor.getValue();
        assertThat(expected.getPaymentSessions(), is(sessionListApi));
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenNoneExist() {
        // given
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PAID.toString());
        final var sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var expected = submissionCaptor.getValue();
        assertThat(expected.getPaymentSessions(), is(sessionListApi));
        assertThat(expected.getStatus(), is(SubmissionStatus.PAYMENT_REQUIRED));
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenPaymentReferenceNotFound() {
        // given
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        final var sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        final Executable actual =
            () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID,
                sessionListApi);

        // then
        final var ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID), ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithPaymentSessionsWhenIncorrectState() {
        // given
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());
        final var sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final Executable actual =
            () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID,
                sessionListApi);

        // then
        final var ex =
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
        final var actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(timestampGenerator, times(2)).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var expected = submissionCaptor.getValue();
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
        when(submission.getFeeOnSubmission()).thenReturn("1");
        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.completeSubmission(SUBMISSION_ID);

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
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        final var exception =
            assertThrows(SubmissionIncorrectStateException.class, () -> submissionService.completeSubmission(SUBMISSION_ID));
        assertEquals("Submission status for [%s] wasn't in [SUBMITTED], couldn't update".formatted(SUBMISSION_ID), exception.getMessage());

        // then
        verify(timestampGenerator, times(2)).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionWhenPaidAfterPaymentPatch() throws SubmissionValidationException {
        // given
        when(submission.getFeeOnSubmission()).thenReturn("1");
        when(submission.getStatus()).thenReturn(SubmissionStatus.SUBMITTED);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual = submissionService.completeSubmission(SUBMISSION_ID);

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
        final var actual = submissionService.completeSubmission(SUBMISSION_ID);

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
        final Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        final var ex = assertThrows(SubmissionValidationException.class, actual);
        assertEquals("bad data", ex.getMessage());
        verify(validator).validate(submission);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionNotFound() {
        // given
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        final Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        final var ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID), ex.getMessage());
        verifyNoInteractions(validator);
        verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteSubmissionIncorrectState() {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.REJECTED_BY_VIRUS_SCAN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        final var ex = assertThrows(SubmissionIncorrectStateException.class, actual);
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
        final var actual = submissionService.updateSubmissionQueued(submission);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.PROCESSING));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));

    }

    @Test
    void testReadSubmission() {
        // given
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submissionMapper.map(submission)).thenReturn(submissionApi);
        // when
        final var actual = submissionService.readSubmission(SUBMISSION_ID);
        // then
        assertEquals(submissionApi, actual);
        verify(submissionRepository).read(SUBMISSION_ID);
        verify(submissionMapper).map(submission);
    }

    @Test
    void testReadSubmissionDoesNotMapMissingSubmission() {
        // when
        final var actual = submissionService.readSubmission(SUBMISSION_ID);

        // then
        assertNull(actual);
        verify(submissionRepository).read(SUBMISSION_ID);
        verifyNoInteractions(submissionMapper);
    }

    @Test
    void testUpdateSubmissionBarcode() {
        // given
        final var barcode = "Y1234ABCD";
        // when
        final var actual = submissionService.updateSubmissionBarcode(SUBMISSION_ID, barcode);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateBarcode(SUBMISSION_ID, barcode);
    }

    @Test
    void testUpdateSubmissionStatus() {
        // when
        final var actual = submissionService.updateSubmissionStatus(SUBMISSION_ID,
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
        final var actual = submissionService.updateSubmissionConfirmAuthorised(SUBMISSION_ID, true);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(argThat(s->s.getConfirmAuthorised().equals(Boolean.TRUE)));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenNotFound() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.FAILED);

        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(null);

        // when
        final var exception =
            assertThrows(SubmissionNotFoundException.class,
                () -> submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID,
                    paymentClose));

        // then
        assertThat(exception.getMessage(), is("Could not locate submission with id: [%s]".formatted(SUBMISSION_ID)));
        verifyNoMoreInteractions(submission);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenIncorrectStatus() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.FAILED);
        final var paySession =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_FAILED, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(),
            is(STATUS_FAILED));
        verifyNoMoreInteractions(submission, emailService);

    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaidBeforeCompletion() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.PAID);
        final var paySession =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(),
            is(STATUS_PAID));
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
        verifyNoMoreInteractions(emailService);

    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenFailedBeforeCompletion() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.FAILED);
        final var paySession =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.OPEN, paySession);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(submission.getId()).thenReturn(SUBMISSION_ID);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(),
            is(STATUS_FAILED));
        verifyNoMoreInteractions(submission, emailService);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentSessionNotMatched() {
        // given
        final var paymentClose =
            new PaymentClose(PAY_SESSION_ID + "X", PaymentClose.Status.FAILED);
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.OPEN, sessionApi);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);

        // when
        final var exception =
            assertThrows(SubmissionIncorrectStateException.class,
                () -> submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID,
                    paymentClose));

        assertThat(exception.getMessage(), is("payment reference not matched"));
        verifyNoInteractions(emailService);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndPaid() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.PAID);
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.PAYMENT_REQUIRED)
            .thenReturn(SubmissionStatus.SUBMITTED);
        when(submissionRepository.read(SUBMISSION_ID)).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(), is(STATUS_PAID));
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submissionCaptor.capture());
        final var updateSubmission = submissionCaptor.getValue();
        assertThat(updateSubmission.getStatus(), is(SubmissionStatus.SUBMITTED));
        assertThat(updateSubmission.getSubmittedAt(), is(NOW));
        assertThat(updateSubmission.getLastModifiedAt(), is(NOW));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndFailed() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.FAILED);
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        when(timestampGenerator.generateTimestamp()).thenReturn(NOW);


        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(), is(STATUS_FAILED));
        verify(submissionRepository).read(SUBMISSION_ID);
        verify(submissionRepository).updateSubmission(argThat(s->s.getStatus() == SubmissionStatus.PAYMENT_FAILED));
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenSessionMatchedAndCancelled() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.CANCELLED);
        final var sessionApi =
                new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual =
                submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(), is(STATUS_CANCELLED));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verify(submissionRepository).read(SUBMISSION_ID);
        verifyNoMoreInteractions(submissionRepository);
        verifyNoInteractions(timestampGenerator);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentAlreadyFailed() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.FAILED);
        final var sessionApi =
                new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentClose.Status.FAILED.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_FAILED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual =
                submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        assertThat(submission.getPaymentSessions().getFirst().getSessionStatus(), is(STATUS_FAILED));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoMoreInteractions(submissionRepository);
    }

    @Test
    void updateSubmissionWithPaymentOutcomeWhenPaymentError() {
        // given
        final var paymentClose = new PaymentClose(PAY_SESSION_ID, PaymentClose.Status.ERROR);
        final var sessionApi =
            new SessionApi(PAY_SESSION_ID, PAY_SESSION_STATE, PaymentTemplate.Status.PENDING.toString());

        expectSubmissionWithPaymentSession(SubmissionStatus.PAYMENT_REQUIRED, sessionApi);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        final var actual =
            submissionService.updateSubmissionWithPaymentOutcome(SUBMISSION_ID, paymentClose);

        assertThat(actual.getId(), is(SUBMISSION_ID));
        verify(submissionRepository, never()).updateSubmission(any(Submission.class));
        verifyNoMoreInteractions(submissionRepository);
    }

    private void expectSubmissionWithPaymentSession(final SubmissionStatus submissionStatus,
        final SessionApi sessionApi) {
        final var sessionListApi =
            new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(submissionStatus);
        when(submission.getPaymentSessions()).thenReturn(sessionListApi);
    }
}