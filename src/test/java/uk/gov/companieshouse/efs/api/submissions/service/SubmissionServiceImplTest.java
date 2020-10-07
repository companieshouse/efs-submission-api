package uk.gov.companieshouse.efs.api.submissions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import uk.gov.companieshouse.efs.api.submissions.mapper.CompanyMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.FileDetailsMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.PaymentSessionMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.PresenterMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionMapper;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.PaymentSession;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.Validator;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceImplTest {
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
    private PaymentSessionMapper paymentSessionMapper;
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
    private EmailService emailService;

    private static final String SUBMISSION_ID = "123";

    @BeforeEach
    public void setUp() {
        submissionService =
            new SubmissionServiceImpl(submissionRepository, submissionMapper, presenterMapper, companyMapper,
                fileDetailsMapper, paymentSessionMapper, timestampGenerator, confirmationReferenceGenerator,
                formTemplateService, emailService, validator);
    }

    @Test
    public void testCreateSubmission() {
        // given
        PresenterApi presenterApi = Mockito.mock(PresenterApi.class);
        Presenter presenter = Mockito.mock(Presenter.class);
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
    public void testUpdateSubmissionWithCompany() {
        // given
        CompanyApi companyApi = Mockito.mock(CompanyApi.class);
        Company company = Mockito.mock(Company.class);
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
    public void testUpdateSubmissionWithCompanyNotFound() {
        // given
        CompanyApi companyApi = Mockito.mock(CompanyApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithCompany(SUBMISSION_ID, companyApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(companyMapper);
    }


    @Test
    public void testUpdateSubmissionWithCompanyIncorrectState() {
        // given
        CompanyApi companyApi = Mockito.mock(CompanyApi.class);
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
    public void testUpdateSubmissionWithForm() {
        // given
        FormTypeApi formApi = Mockito.mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmission(submission);
    }


    @Test
    public void testUpdateSubmissionWithFormWhereFormDetailsAlreadyExist() {
        // given
        FormTypeApi formApi = Mockito.mock(FormTypeApi.class);
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
    public void testUpdateSubmissionWithFormNotFound() {
        // given
        FormTypeApi formApi = Mockito.mock(FormTypeApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
    }

    @Test
    public void testUpdateSubmissionWithFormIncorrectState() {
        // given
        FormTypeApi formApi = Mockito.mock(FormTypeApi.class);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithForm(SUBMISSION_ID, formApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals("Submission status for [123] wasn't OPEN, couldn't update", ex.getMessage());
    }

    @Test
    public void testUpdateSubmissionWithFiles() {
        // given
        FileListApi fileListApi = Mockito.mock(FileListApi.class);
        List<FileDetails> fileDetailsList = Collections.singletonList(Mockito.mock(FileDetails.class));
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
    public void testUpdateSubmissionWithFilesWhereFormDetailsAlreadyExist() {
        // given
        FileListApi fileListApi = Mockito.mock(FileListApi.class);
        List<FileDetails> fileDetailsList = Collections.singletonList(Mockito.mock(FileDetails.class));
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
    public void testUpdateSubmissionWithFilesNotFound() {
        // given
        FileListApi fileListApi = Mockito.mock(FileListApi.class);
        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileListApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(fileDetailsMapper);
    }

    @Test
    public void testUpdateSubmissionWithFilesIncorrectState() {
        // given
        FileListApi fileListApi = Mockito.mock(FileListApi.class);
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
    public void testUpdateSubmissionWithPaymentSessions() {
        // given
        SessionApi sessionApi = new SessionApi(SESSION_ID, SESSION_STATE);
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(paymentSessionMapper.map(sessionApi)).thenReturn(new PaymentSession(SESSION_ID, SESSION_STATE));
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);
        when(submissionRepository.read(anyString())).thenReturn(submission);

        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(paymentSessionMapper).map(sessionApi);
        verify(submissionRepository).updateSubmission(submission);
    }

    @Test
    public void testUpdateSubmissionWithPaymentReferenceNotFound() {
        // given
        SessionApi sessionApi = new SessionApi(SESSION_ID, SESSION_STATE);
        SessionListApi sessionListApi = new SessionListApi(Collections.singletonList(sessionApi));

        when(submissionRepository.read(anyString())).thenReturn(null);

        // when
        Executable actual = () -> submissionService.updateSubmissionWithPaymentSessions(SUBMISSION_ID, sessionListApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verifyNoInteractions(paymentSessionMapper);
    }

    @Test
    public void testUpdateSubmissionWithPaymentReferenceIncorrectState() {
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
        verifyNoInteractions(paymentSessionMapper);
    }

    @Test
    public void testCompleteSubmission() throws SubmissionValidationException {
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
    public void testCompleteSubmissionValidationFailure() throws SubmissionValidationException {
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
    public void testCompleteSubmissionNotFound() {
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
    public void testCompleteSubmissionIncorrectState() {
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
    public void testUpdateSubmissionQueued() {
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
    public void testReadSubmission() {
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
    public void testReadSubmissionDoesNotMapMissingSubmission() {
        // when
        SubmissionApi actual = submissionService.readSubmission(SUBMISSION_ID);

        // then
        assertNull(actual);
        verify(submissionRepository).read(SUBMISSION_ID);
        verifyNoInteractions(submissionMapper);
    }

    @Test
    public void testUpdateSubmissionBarcode() {
        // given
        String barcode = "Y1234ABCD";
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionBarcode(SUBMISSION_ID, barcode);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateBarcode(SUBMISSION_ID, barcode);
    }

    @Test
    public void testUpdateSubmissionStatus() {
        // when
        SubmissionResponseApi actual = submissionService.updateSubmissionStatus(SUBMISSION_ID,
                SubmissionStatus.ACCEPTED);
        // then
        assertEquals(SUBMISSION_ID, actual.getId());
        verify(submissionRepository).updateSubmissionStatus(SUBMISSION_ID, SubmissionStatus.ACCEPTED);
    }

    @Test
    public void testUpdateSubmission() {
        // when
        submissionService.updateSubmission(submission);
        // then
        verify(timestampGenerator).generateTimestamp();
        verify(submissionRepository).updateSubmission(submission);
    }

    @Test
    public void testUpdateSubmissionConfirmAuthorised() {
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