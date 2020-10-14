package uk.gov.companieshouse.efs.api.submissions.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
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
    public static final String SESSION_ID = "2222222222";
    public static final String SESSION_STATE = "FD_RlzcLp-xcK1YZGEbn3ZpRHGlwy7tNjn_zsjYVauoB8Ml3GkfpmbhPuPd093XM";

    private SubmissionService submissionService;

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

    private static final String SUBMISSION_ID = "123";

    @BeforeEach
    public void setUp() {
        submissionService =
            new SubmissionServiceImpl(submissionRepository, submissionMapper, presenterMapper, companyMapper,
                fileDetailsMapper, timestampGenerator, confirmationReferenceGenerator, formTemplateService,
                paymentTemplateService, emailService, validator);
    }

    @Test
    void testCreateSubmission() {
        // given
        PresenterApi presenterApi = mock(PresenterApi.class);
        Presenter presenter = mock(Presenter.class);
        when(presenterMapper.map(presenterApi)).thenReturn(presenter);

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
        verify(submissionRepository).updateSubmission(submission);
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
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
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
        verify(submissionRepository).updateSubmission(submission);
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
        verify(submissionRepository).updateSubmission(submission);
        verify(submission, never()).setFeeOnSubmission(anyString());
        verifyNoInteractions(formTemplateService, paymentTemplateService);
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
        verify(submissionRepository).updateSubmission(submission);
        verify(submission, never()).setFeeOnSubmission(anyString());
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
        verify(submissionRepository).updateSubmission(submission);
        verify(submission, never()).setFeeOnSubmission(anyString());
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
        verify(submissionRepository).updateSubmission(submission);
        verify(submission, never()).setFeeOnSubmission(anyString());
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
        verify(submissionRepository).updateSubmission(submission);
        verify(submission).setFeeOnSubmission(PAYMENT_CHARGE);
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
        verify(submissionRepository).updateSubmission(submission);
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
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
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
        verify(submissionRepository).updateSubmission(submission);
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
        verify(submissionRepository).updateSubmission(submission);
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
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    void testUpdateSubmissionWithPaymentSessions() {
        // given
        SessionApi sessionApi = new SessionApi(SESSION_ID, SESSION_STATE);
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submission);
    }

    @Test
    void testUpdateSubmissionWithPaymentReferenceNotFound() {
        // given
        SessionApi sessionApi = new SessionApi(SESSION_ID, SESSION_STATE);
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
    }

    @Test
    void testUpdateSubmissionWithPaymentReferenceIncorrectState() {
        // given
        SessionApi sessionApi = new SessionApi(SESSION_ID, SESSION_STATE);
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
    }

    @Test
    void testCompleteSubmission() throws SubmissionValidationException {
        // given
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);
        LocalDateTime now = LocalDateTime.now();
        when(timestampGenerator.generateTimestamp()).thenReturn(now.minusSeconds(1L)).thenReturn(now);
        // when
        SubmissionResponseApi actual = submissionService.completeSubmission(SUBMISSION_ID);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(timestampGenerator, times(2)).generateTimestamp();
        verify(submissionRepository).updateSubmission(submission);
        verify(submission).setSubmittedAt(now.minusSeconds(1L));
        verify(submission).setLastModifiedAt(now);
        verify(validator).validate(submission);
        verify(emailService).sendExternalConfirmation(new ExternalConfirmationEmailModel(submission));
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
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.completeSubmission(SUBMISSION_ID);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
        verifyNoInteractions(validator);
        verifyNoInteractions(emailService);
    }

    @Test
    void testUpdateSubmissionQueued() {
        // given
        LocalDateTime now = LocalDateTime.now();
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getId()).thenReturn(SUBMISSION_ID);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionQueued(submission);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submission);
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
        // when
        submissionService.updateSubmission(submission);
        // then
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submission);
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
        verify(submissionRepository).updateSubmission(submission);
    }

}