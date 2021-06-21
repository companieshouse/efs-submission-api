package uk.gov.companieshouse.efs.api.events.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.events.FileConversionResultStatusApi;
import uk.gov.companieshouse.api.model.efs.events.FileConversionStatusApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.exception.FesLoaderException;
import uk.gov.companieshouse.efs.api.events.service.exception.InvalidTiffException;
import uk.gov.companieshouse.efs.api.events.service.exception.TiffDownloadException;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.events.service.model.FesFileModel;
import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.FileNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final int NUMBER_OF_PAGES = 100;
    private static final String SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT = "dd MMMM yyyy";
    private static final String SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT = "dd/MM/yyyy";

    private EventServiceImpl eventService;

    @Mock
    private SubmissionRepository repository;

    @Mock
    private Decision decision;

    @Mock
    private Submission submission;

    @Mock
    private Submission failedSubmission;

    @Mock
    private Company company;

    @Mock
    private SubmissionService submissionService;
    
    @Mock
    private FormTemplateService formTemplateService;

    @Mock
    private FormDetails formDetails;

    @Mock
    private FileDetails fileDetails;

    @Mock
    private EmailService emailService;

    @Mock
    private CurrentTimestampGenerator currentTimestampGenerator;

    @Mock
    private DecisionEngine decisionEngine;

    @Mock
    private BarcodeGeneratorService barcodeGeneratorService;

    @Mock
    private TiffDownloadService tiffDownloadService;

    @Mock
    private FesLoaderService fesLoaderService;

    @Mock
    private ExecutionEngine executionEngine;

    @Mock
    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;

    private int supportHours = 6;

    private int businessHours = 12;

    @Mock
    private DelayedSubmissionSupportEmailModel delayedSubmissionSupportEmailModel;

    @BeforeEach
    void setUp() {
        this.eventService = new EventServiceImpl(submissionService, formTemplateService, emailService, repository,
            currentTimestampGenerator, 50, decisionEngine, barcodeGeneratorService, tiffDownloadService,
            fesLoaderService, executionEngine, formCategoryToEmailAddressService, supportHours, businessHours);
    }

    @Test
    void testEventServiceReturnsSubmittedSubmissionsFromRepository() {
        //given
        List<Submission> expectedSubmissions = Collections.singletonList(submission);
        when(repository.findByStatus(SubmissionStatus.SUBMITTED, 50)).thenReturn(expectedSubmissions);

        //when
        List<Submission> actual = eventService.findSubmissionsByStatus(SubmissionStatus.SUBMITTED);

        //then
        assertEquals(expectedSubmissions, actual);
        verify(repository).findByStatus(SubmissionStatus.SUBMITTED, 50);
    }

    @Test
    void testUpdateFileConversionStatusLastFileConverted() {
        //given
        LocalDateTime now = LocalDateTime.now();
        FileDetails details = FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        when(repository.read(anyString())).thenReturn(submission);
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(details)).build());
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        //when
        eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        //then
        assertEquals(FileConversionStatus.CONVERTED, details.getConversionStatus());
        assertEquals(NUMBER_OF_PAGES, details.getNumberOfPages());
        assertEquals("999", details.getConvertedFileId());
        verify(submission).setStatus(SubmissionStatus.READY_TO_SUBMIT);
        verify(repository).updateSubmission(submission);
        verify(currentTimestampGenerator).generateTimestamp();
        verifyNoInteractions(emailService);
    }

    @Test
    void testUpdateFileConversionStatusFilesPending() {
        //given
        FileDetails details = FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        FileDetails otherDetails = FileDetails.builder()
                .withFileId("abd")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        LocalDateTime now = LocalDateTime.now();
        when(repository.read(anyString())).thenReturn(submission);
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Arrays.asList(details, otherDetails)).build());
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        //when
        eventService.updateConversionFileStatus("123", "abd", fileConversionStatusApi);

        //then
        assertEquals(FileConversionStatus.CONVERTED, otherDetails.getConversionStatus());
        assertEquals(NUMBER_OF_PAGES, otherDetails.getNumberOfPages());
        assertEquals("999", otherDetails.getConvertedFileId());
        verify(submission, times(0)).setStatus(SubmissionStatus.READY_TO_SUBMIT);
        verify(repository).updateSubmission(submission);
        verify(currentTimestampGenerator).generateTimestamp();
        verifyNoInteractions(emailService);
    }

    @Test
    void testUpdateFileConversionStatusLastFileConvertedHasFailed() {
        //given
        FileDetails details = FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        LocalDateTime now = LocalDateTime.now();
        when(repository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(details)).build());
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi(null, FileConversionResultStatusApi.FAILED,
                null);

        //when
        eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        //then
        assertEquals(FileConversionStatus.FAILED, details.getConversionStatus());
        assertNull(details.getConvertedFileId());
        assertNull(details.getNumberOfPages());
        verify(submission).setStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER);
        verify(repository).updateSubmission(submission);
        verify(emailService).sendInternalFailedConversion(any(InternalFailedConversionModel.class));
        verify(currentTimestampGenerator).generateTimestamp();
    }

    @Test
    void testUpdateFileConversionStatusLastFileConvertedHasFailedFirstFileConverted() {
        //given
        FileDetails details = FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.CONVERTED)
                .build();
        FileDetails otherDetails = FileDetails.builder()
                .withFileId("abd")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        LocalDateTime now = LocalDateTime.now();
        when(repository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Arrays.asList(details, otherDetails)).build());
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi(null, FileConversionResultStatusApi.FAILED,
                null);

        //when
        eventService.updateConversionFileStatus("123", "abd", fileConversionStatusApi);

        //then
        assertEquals(FileConversionStatus.FAILED, otherDetails.getConversionStatus());
        assertNull(details.getNumberOfPages());
        assertNull(details.getConvertedFileId());
        verify(submission).setStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER);
        verify(repository).updateSubmission(submission);
        verify(emailService).sendInternalFailedConversion(any(InternalFailedConversionModel.class));
        verify(currentTimestampGenerator).generateTimestamp();
    }

    @Test
    void testUpdateFileConversionStatusLastFileConvertedHasFailedAllFilesQueued() {
        //given
        FileDetails details = FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        FileDetails otherDetails = FileDetails.builder()
                .withFileId("abd")
                .withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        LocalDateTime now = LocalDateTime.now();
        when(repository.read(anyString())).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Arrays.asList(details, otherDetails)).build());
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi(null, FileConversionResultStatusApi.FAILED,
                null);

        //when
        eventService.updateConversionFileStatus("123", "abd", fileConversionStatusApi);

        //then
        assertEquals(FileConversionStatus.FAILED, otherDetails.getConversionStatus());
        assertNull(details.getNumberOfPages());
        assertNull(details.getConvertedFileId());
        verify(submission, times(0)).setStatus(any());
        verify(repository).updateSubmission(submission);
        verifyNoInteractions(emailService);
        verify(currentTimestampGenerator).generateTimestamp();
    }

    @Test
    void testUpdateFileConversionStatusLastFileConvertedHasFailedAndEmailServiceThrowsException() {
        // given
        FileDetails details = FileDetails.builder().withFileId("abc").withConversionStatus(FileConversionStatus.QUEUED)
                .build();
        LocalDateTime now = LocalDateTime.now();

        when(repository.read(anyString())).thenReturn(submission);
        when(submission.getFormDetails())
                .thenReturn(FormDetails.builder().withFileDetailsList(Collections.singletonList(details)).build());
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi(null,
                FileConversionResultStatusApi.FAILED, null);
        doThrow(EmailServiceException.class).when(emailService).sendInternalFailedConversion(any());

        // when
        eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        // then
        assertEquals(FileConversionStatus.FAILED, details.getConversionStatus());
        assertNull(details.getNumberOfPages());
        assertNull(details.getConvertedFileId());
        verify(submission).setStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER);
        verify(repository).updateSubmission(submission);
        verify(emailService).sendInternalFailedConversion(any(InternalFailedConversionModel.class));
        verify(currentTimestampGenerator).generateTimestamp();
    }

    @Test
    void testUpdateFileConversionStatusThrowsSubmissionNotFoundException() {
        // given
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        when(repository.read("123")).thenReturn(null);
        // when
        Executable actual = () -> eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        // then
        SubmissionNotFoundException ex = assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals("Could not locate submission with id: [123]", ex.getMessage());
        verify(repository).read("123");
    }

    @Test
    void testUpdateFileConversionStatusThrowsSubmissionIncorrectStateException() {
        // given
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        when(submission.getStatus()).thenReturn(SubmissionStatus.SUBMITTED);
        when(repository.read("123")).thenReturn(submission);
        // when
        Executable actual = () -> eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        // then
        SubmissionIncorrectStateException ex = assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals("Submission status for [123] wasn't [PROCESSING], couldn't update", ex.getMessage());
        verify(repository).read("123");
    }

    @Test
    void testUpdateFileConversionStatusThrowsFileNotFoundException() {
        // given
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(
                FileDetails.builder().withFileId("bcd").withConversionStatus(FileConversionStatus.QUEUED).build()));
        when(repository.read("123")).thenReturn(submission);
        // when
        Executable actual = () -> eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        // then
        FileNotFoundException ex = assertThrows(FileNotFoundException.class, actual);
        assertEquals("Could not locate file with id [abc] on submission with id: [123]", ex.getMessage());
        verify(repository).read("123");
    }

    @Test
    void testUpdateFileConversionStatusThrowsFileIncorrectStateException() {
        // given
        FileConversionStatusApi fileConversionStatusApi = new FileConversionStatusApi("999",
                FileConversionResultStatusApi.CONVERTED, NUMBER_OF_PAGES);

        when(submission.getStatus()).thenReturn(SubmissionStatus.PROCESSING);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(
                FileDetails.builder().withFileId("abc").withConversionStatus(FileConversionStatus.WAITING).build()));
        when(repository.read("123")).thenReturn(submission);
        // when
        Executable actual = () -> eventService.updateConversionFileStatus("123", "abc", fileConversionStatusApi);

        // then
        FileIncorrectStateException ex = assertThrows(FileIncorrectStateException.class, actual);
        assertEquals("Status for file with id [abc] wasn't [QUEUED] on submission with id [123], couldn't update",
                ex.getMessage());
        verify(repository).read("123");
    }

    @Test
    void testSubmitToFes() {
        //given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(submission.getId()).thenReturn("1234abcd");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getCompany()).thenReturn(company);
        when(submission.getSubmittedAt()).thenReturn(now);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));

        //when
        eventService.submitToFes();

        //then
        verify(submissionService).updateSubmissionBarcode("1234abcd", "Y123XYZ");
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(barcodeGeneratorService, times(1)).getBarcode(now);
        verify(tiffDownloadService).downloadTiffFile(convertedFileId);
        verify(fesLoaderService).insertSubmission(new FesLoaderModel("Y123XYZ", "abc", "1223456",
                "SH01", Collections.singletonList(new FesFileModel(null, 0)), now));
        verify(submissionService).updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);
    }

    @Test
    void testSubmitToFesWithMappedFesDocType() {
        //given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(submission.getId()).thenReturn("1234abcd");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getCompany()).thenReturn(company);
        when(submission.getSubmittedAt()).thenReturn(now);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, "FES-DOC-TYPE", null));

        //when
        eventService.submitToFes();

        //then
        verify(submissionService).updateSubmissionBarcode("1234abcd", "Y123XYZ");
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(barcodeGeneratorService, times(1)).getBarcode(now);
        verify(tiffDownloadService).downloadTiffFile(convertedFileId);
        verify(fesLoaderService).insertSubmission(new FesLoaderModel("Y123XYZ", "abc", "1223456",
                "FES-DOC-TYPE", Collections.singletonList(new FesFileModel(null, 0)), now));
        verify(submissionService).updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);
    }

    @Test
    void testSubmitToFesWhenNoSubmittedAtDate() {
        //given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(submission.getId()).thenReturn("1234abcd");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getCompany()).thenReturn(company);
        when(submission.getCreatedAt()).thenReturn(now);
        when(submission.getSubmittedAt()).thenReturn(null);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));

        //when
        eventService.submitToFes();

        //then
        verify(submissionService).updateSubmissionBarcode("1234abcd", "Y123XYZ");
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(barcodeGeneratorService, times(1)).getBarcode(now);
        verify(tiffDownloadService).downloadTiffFile(convertedFileId);
        verify(fesLoaderService).insertSubmission(new FesLoaderModel("Y123XYZ", "abc", "1223456",
                "SH01", Collections.singletonList(new FesFileModel(null, 0)), now));
        verify(submissionService).updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);
    }


    @Test
    void testGetBarcodeException() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(barcodeGeneratorService.getBarcode(any())).thenThrow(BarcodeException.class);

        //when
        eventService.submitToFes();

        //then
        verifyNoInteractions(submissionService, fesLoaderService, tiffDownloadService);
    }

    @Test
    void testTiffDownloadException() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("1234abcd");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("SH01");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(tiffDownloadService.downloadTiffFile(any())).thenThrow(TiffDownloadException.class);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));

        //when
        eventService.submitToFes();

        //then
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(submissionService).updateSubmissionBarcode(submission.getId(), "Y123XYZ");
        verifyNoInteractions(fesLoaderService);
        verifyNoMoreInteractions(submissionService);
    }

    @Test
    void formTemplateMissing() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("1234abcd");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("SH01");
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");

        //when
        eventService.submitToFes();

        //then
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(submissionService).updateSubmissionBarcode(submission.getId(), "Y123XYZ");
        verifyNoInteractions(fesLoaderService, tiffDownloadService);
        verifyNoMoreInteractions(submissionService);
    }

    @Test
    void testHandlesFesLoaderException() {
        //given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);

        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");


        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));

        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);

        doThrow(FesLoaderException.class).when(fesLoaderService).insertSubmission(any());

        // when
        eventService.submitToFes();

        // then
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(submissionService).updateSubmissionBarcode(submission.getId(), "Y123XYZ");
        verify(fesLoaderService).insertSubmission(any());
        verifyNoMoreInteractions(submissionService);
    }

    @Test
    void testHandlesInvalidTiffException() {
        // given
        String convertedFileId = "1234";
        when(submission.getId()).thenReturn("1234abcd");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));
        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");

        doThrow(InvalidTiffException.class).when(fesLoaderService).insertSubmission(any());
        // when
        eventService.submitToFes();

        // then
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(submissionService).updateSubmissionBarcode("1234abcd", "Y123XYZ");
        verify(fesLoaderService).insertSubmission(any());
        verifyNoMoreInteractions(submissionService);
    }

    @Test
    void testHandlesMultipleSubmissionsWhenOneFailsWithBarcodeException() {
        // given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";

        when(repository.findByStatus(any(), anyInt())).thenReturn(getSubmissionList());
        when(submission.getId()).thenReturn("1234abcd");
        when(barcodeGeneratorService.getBarcode(any())).thenReturn("Y123XYZ").thenThrow(BarcodeException.class);

        when(submission.getFormDetails()).thenReturn(formDetails);
        when(failedSubmission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));
        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);

        when(submission.getSubmittedAt()).thenReturn(now);
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");

        // when
        eventService.submitToFes();

        // then
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verify(barcodeGeneratorService, times(2)).getBarcode(any());
        verify(submissionService, times(1)).updateSubmissionBarcode("1234abcd", "Y123XYZ");
        verify(tiffDownloadService, times(1)).downloadTiffFile(convertedFileId);
        verify(fesLoaderService, times(1)).insertSubmission(
                new FesLoaderModel("Y123XYZ", "abc", "1223456", "SH01",
                        Collections.singletonList(new FesFileModel(null, 0)), now));
        verify(submissionService, times(1)).updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);
    }

    @Test
    void testControllerDoesNotCallBarcodeServiceAgainIfSubmissionAlreadyHasBarcode() {
        //given
        LocalDateTime now = LocalDateTime.now();
        String convertedFileId = "1234";
        when(submission.getId()).thenReturn("1234abcd");
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(submission.getSubmittedAt()).thenReturn(now);
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyName()).thenReturn("abc");
        when(company.getCompanyNumber()).thenReturn("1223456");

        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(formDetails.getFormType()).thenReturn("SH01");
        when(formDetails.getBarcode()).thenReturn("Y9999999");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(
            new FormTemplateApi("SH01", "formName", "category", "", false, true, null, null));

        when(fileDetails.getConvertedFileId()).thenReturn(convertedFileId);

        //when
        eventService.submitToFes();

        //then
        verify(submissionService, times(0)).updateSubmissionBarcode(any(), any());
        verify(repository).findByStatus(SubmissionStatus.READY_TO_SUBMIT, 50);
        verifyNoInteractions(barcodeGeneratorService);
        verify(tiffDownloadService).downloadTiffFile(convertedFileId);
        verify(fesLoaderService).insertSubmission(new FesLoaderModel("Y9999999", "abc", "1223456",
                "SH01", Collections.singletonList(new FesFileModel(null, 0)), now));
        verify(submissionService).updateSubmissionStatus(submission.getId(), SubmissionStatus.SENT_TO_FES);
    }

    @Test
    void testProcessSubmissions() {
        //given
        when(repository.findByStatus(any(), anyInt())).thenReturn(Collections.singletonList(submission));
        when(decisionEngine.evaluateSubmissions(any())).thenReturn(Collections.singletonMap(DecisionResult.FES_ENABLED, Collections.singletonList(decision)));

        //when
        eventService.processFiles();

        //then
        verify(repository).findByStatus(SubmissionStatus.SUBMITTED, 50);
        verify(decisionEngine).evaluateSubmissions(Collections.singletonList(submission));
        verify(executionEngine).execute(Collections.singletonMap(DecisionResult.FES_ENABLED, Collections.singletonList(decision)));
    }

    @Test
    void testHandleNoDelayedSubmissions(){
        // given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Collections.emptyList());

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
    }

    @Test
    void testHandleDelayedSubmissionsSendSupportNotification(){
        // given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getLastModifiedAt()).thenReturn(now.minusHours(supportHours));
        when(submission.getSubmittedAt()).thenReturn(now.minusHours(supportHours).minusSeconds(5));

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
        verify(emailService).sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(Collections.singletonList(new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(supportHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))));
        verify(emailService, times(0)).sendDelayedSubmissionBusinessEmail(any(DelayedSubmissionBusinessEmailModel.class));
    }

    @Test
    void testHandleDelayedSubmissionsSendSupportNotificationSubmittedAtMissing(){
        // given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getLastModifiedAt()).thenReturn(now.minusHours(supportHours));
        when(submission.getCreatedAt()).thenReturn(now.minusHours(supportHours).minusSeconds(5));

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
        verify(emailService).sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(Collections.singletonList(new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(supportHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))));
        verify(emailService, times(0)).sendDelayedSubmissionBusinessEmail(any(DelayedSubmissionBusinessEmailModel.class));
    }

    @Test
    void testHandleDelayedSubmissionsSendBusinessNotification() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("12345678", "ACME"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder().withFormType("CC01").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(1));
        when(submission.getSubmittedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn("cc@ch.gov.uk");

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
        verify(emailService).sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(Collections.singletonList(new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))));
        verify(emailService).sendDelayedSubmissionBusinessEmail(new DelayedSubmissionBusinessEmailModel(Collections.singletonList(new DelayedSubmissionBusinessModel("345efg", "12345678", "CC01", "demo@ch.gov.uk", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT)))), "cc@ch.gov.uk", businessHours));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
    }

    @Test
    void testHandleDelayedSubmissionsSendBusinessNotificationSubmittedAtMissing() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Collections.singletonList(submission));
        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("12345678", "ACME"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder().withFormType("CC01").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(1));
        when(submission.getCreatedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn("cc@ch.gov.uk");

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
        verify(emailService).sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(Collections.singletonList(new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))));
        verify(emailService).sendDelayedSubmissionBusinessEmail(new DelayedSubmissionBusinessEmailModel(Collections.singletonList(new DelayedSubmissionBusinessModel("345efg", "12345678", "CC01", "demo@ch.gov.uk", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT)))), "cc@ch.gov.uk", businessHours));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
    }

    @Test
    void testHandleDelayedSubmissionsSendBusinessNotificationToMultipleUnitsIfDifferentSubmissionCategories() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(currentTimestampGenerator.generateTimestamp()).thenReturn(now);
        when(repository.findDelayedSubmissions(eq(SubmissionStatus.PROCESSING),any())).thenReturn(Arrays.asList(submission, submission, submission));
        when(submission.getId()).thenReturn("123abd");
        when(submission.getConfirmationReference()).thenReturn("345efg");
        when(submission.getCompany()).thenReturn(new Company("12345678", "ACME"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder().withFormType("CC01").build())
                .thenReturn(FormDetails.builder().withFormType("RP03").build())
                .thenReturn(FormDetails.builder().withFormType("CC03").build());
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getLastModifiedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(1));
        when(submission.getSubmittedAt()).thenReturn(now.minusHours(businessHours).minusSeconds(5));
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01")).thenReturn("cc@ch.gov.uk");
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC03")).thenReturn("cc@ch.gov.uk");
        when(formCategoryToEmailAddressService.getEmailAddressForFormCategory("RP03")).thenReturn("rp@ch.gov.uk");

        // when
        eventService.handleDelayedSubmissions();

        // then
        verify(currentTimestampGenerator).generateTimestamp();
        verify(repository).findDelayedSubmissions(SubmissionStatus.PROCESSING, now.minusHours(supportHours));
        verify(emailService, times(1)).sendDelayedSubmissionSupportEmail(new DelayedSubmissionSupportEmailModel(Arrays.asList(new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))), new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))), new DelayedSubmissionSupportModel("123abd", "345efg", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_SUPPORT_EMAIL_DATE_FORMAT))))));
        verify(emailService, times(1)).sendDelayedSubmissionBusinessEmail(new DelayedSubmissionBusinessEmailModel(Arrays.asList(new DelayedSubmissionBusinessModel("345efg", "12345678", "CC01", "demo@ch.gov.uk", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT))), new DelayedSubmissionBusinessModel("345efg", "12345678", "CC03", "demo@ch.gov.uk", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT)))), "cc@ch.gov.uk", businessHours));
        verify(emailService, times(1)).sendDelayedSubmissionBusinessEmail(new DelayedSubmissionBusinessEmailModel(Collections.singletonList(new DelayedSubmissionBusinessModel("345efg", "12345678", "RP03", "demo@ch.gov.uk", now.minusHours(businessHours).minusSeconds(5).format(DateTimeFormatter.ofPattern(SUBMITTED_AT_BUSINESS_EMAIL_DATE_FORMAT)))), "rp@ch.gov.uk", businessHours));
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("CC01");
        verify(formCategoryToEmailAddressService).getEmailAddressForFormCategory("RP03");
    }

    private List<Submission> getSubmissionList() {
        List<Submission> submissions = new ArrayList<>();
        submissions.add(submission);
        submissions.add(failedSubmission);
        return submissions;
    }
}
