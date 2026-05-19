package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filetransfer.AvStatus;
import uk.gov.companieshouse.api.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.filetransfer.FileDetailsException;
import uk.gov.companieshouse.efs.api.filetransfer.FileTransferService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {
    private static final String FILE_TIMESTAMP = "2024-06-01T12:34:56Z";
    private static final String AV_TIMESTAMP = "2024-06-03T12:34:56Z";
    private static final String FILE_ID = "dummy-file-id";
    private static final String FILE_NAME = "dummy-file-name";
    private static final String FILE_CONTENT_TYPE = "application/pdf";
    private static final Long FILE_SIZE = 1024L;
    private static final String FORM_TYPE = "CC01";

    private DecisionEngine decisionEngine;

    @Mock
    private FileTransferService transferService;

    @Mock
    private Submission submission;

    @Mock
    private FormTemplateRepository formTemplateRepository;

    @Mock
    private CurrentTimestampGenerator timestampGenerator;

    @Mock
    private SubmissionService submissionService;
    private FormTemplate formTemplate;
    private FormDetails formDetails;

    @BeforeEach
    void setUp() {
        decisionEngine = new DecisionEngine(transferService, formTemplateRepository, timestampGenerator, submissionService);
        formTemplate = FormTemplate
                           .builder()
                           .withFormType(FORM_TYPE)
                           .withFormName("Test CC01")
                           .withFormCategory("CC")
                           .withFesDocType("FES")
                           .build();
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfFileUnscanned() {
        //given
        final var unscannedResponse = new FileDetailsApi(FILE_ID, null, AvStatus.NOT_SCANNED, FILE_CONTENT_TYPE, FILE_SIZE,
            FILE_NAME, FILE_TIMESTAMP, null);
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);

        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(formTemplate));
        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(unscannedResponse));
        formDetails = FormDetails.builder().withFormType(FORM_TYPE).withFileDetailsList(
            Collections.singletonList(fileDetails)).build();
        when(submission.getFormDetails()).thenReturn(formDetails);

        //when
        final var actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.NO_DECISION), hasSize(1));
        verify(transferService).getFileDetails(FILE_ID);
    }

    @Test
    void testDecisionEngineReturnsNotCleanIfAllFilesScannedOneFileNotClean() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);
        final var infectedFileDetailsApi = new FileDetailsApi(FILE_ID, AV_TIMESTAMP, AvStatus.INFECTED, FILE_TIMESTAMP, FILE_SIZE,
            FILE_NAME, FILE_TIMESTAMP, null);
        final var now = LocalDateTime.now();

        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(formTemplate));
        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(infectedFileDetailsApi));
        formDetails = FormDetails.builder().withFormType(FORM_TYPE).withFileDetailsList(
            Collections.singletonList(fileDetails)).build();
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(timestampGenerator.generateTimestamp()).thenReturn(now);

        //when
        final var actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.NOT_CLEAN), hasSize(1));
        assertThat(fileDetails.getLastModifiedAt(), is(now));
        assertThat(fileDetails.getConversionStatus(), is(FileConversionStatus.FAILED_AV));
        verify(timestampGenerator).generateTimestamp();
        verify(transferService).getFileDetails(FILE_ID);
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfOneFileNotCleanOtherFilesUnscanned() {
        //given
        final var fileDetails = new FileDetailsApi(FILE_ID, null, AvStatus.NOT_SCANNED,
            FILE_CONTENT_TYPE, FILE_SIZE, FILE_NAME, FILE_TIMESTAMP, null);

        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(formTemplate));
        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(fileDetails));
        formDetails = FormDetails.builder().withFormType(FORM_TYPE).withFileDetailsList(
            Arrays.asList(getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.FAILED_AV),
                getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING))).build();
        when(submission.getFormDetails()).thenReturn(formDetails);

        //when
        final var actual = decisionEngine.evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.NO_DECISION), hasSize(1));
        verifyNoInteractions(timestampGenerator, submissionService);
    }

    @Test
    void testDecisionEngineReturnsFesEnabledIfFormTypeEnabledInFes() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);
        final var now = LocalDateTime.now();
        final var cleanFileDetailsApi = new FileDetailsApi(FILE_ID, AV_TIMESTAMP, AvStatus.CLEAN, FILE_CONTENT_TYPE,
            FILE_SIZE, FILE_NAME, FILE_TIMESTAMP, null);

        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(cleanFileDetailsApi));
        formDetails = FormDetails.builder().withFileDetailsList(Collections.singletonList(fileDetails)).withFormType(
            FORM_TYPE).build();
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(
            FormTemplate.builder()
                .withFormType(FORM_TYPE)
                .withFormName("FES enabled CC01")
                .withFormCategory("CC")
                .withFesEnabled(true)
                .withFesDocType("FES")
                .build()));

        //when
        final var actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.FES_ENABLED), hasSize(1));
        assertThat(fileDetails.getLastModifiedAt(), is(now));
        assertThat(fileDetails.getConversionStatus(), is(FileConversionStatus.CLEAN_AV));
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testDecisionEngineReturnsNotFesEnabledIfFormTypeDisabledInFes() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);
        final var now = LocalDateTime.now();
        final var cleanFileDetailsApi = new FileDetailsApi(FILE_ID, AV_TIMESTAMP, AvStatus.CLEAN, FILE_CONTENT_TYPE,
            FILE_SIZE, FILE_NAME, FILE_TIMESTAMP, null);

        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(cleanFileDetailsApi));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .withFormType(FORM_TYPE)
                .build());
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(
            FormTemplate.builder()
                .withFormType(FORM_TYPE)
                .withFormName("Change of address")
                .withFormCategory("CC")
                .withFesDocType("FES")
                .build()));

        //when
        final var actual = decisionEngine.evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.NOT_FES_ENABLED), hasSize(1));
        assertThat(fileDetails.getLastModifiedAt(), is(now));
        assertThat(fileDetails.getConversionStatus(), is(FileConversionStatus.CLEAN_AV));
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testDecisionEngineReturnsFormTypeDoesNotExistIfFormUnhandled() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);
        final var now = LocalDateTime.now();
        final var cleanFileDetailsApi = new FileDetailsApi(FILE_ID, AV_TIMESTAMP, AvStatus.CLEAN, FILE_CONTENT_TYPE,
            FILE_SIZE, FILE_NAME, FILE_TIMESTAMP, null);

        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.of(cleanFileDetailsApi));
        formDetails = FormDetails.builder().withFileDetailsList(Collections.singletonList(fileDetails)).withFormType(
            FORM_TYPE).build();
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.empty());

        //when
        final var actual = decisionEngine.evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.FORM_TYPE_DOES_NOT_EXIST), hasSize(1));
        assertThat(fileDetails.getLastModifiedAt(), is(now));
        assertThat(fileDetails.getConversionStatus(), is(FileConversionStatus.CLEAN_AV));
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfFileTransferApiReturns404() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);

        when(formTemplateRepository.findById(FORM_TYPE)).thenReturn(Optional.of(formTemplate));
        when(transferService.getFileDetails(FILE_ID)).thenReturn(Optional.empty());
        formDetails = FormDetails.builder().withFileDetailsList(Collections.singletonList(fileDetails)).withFormType(
            FORM_TYPE).build();
        when(submission.getFormDetails()).thenReturn(formDetails);

        //when
        final var actual = decisionEngine.evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertThat(actual.get(DecisionResult.NO_DECISION), hasSize(1));
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfFileTransferApiReturnsUnexpectedStatus() {
        //given
        final var fileDetails = getExpectedFileDetailsWaiting(FILE_ID, FileConversionStatus.WAITING);

        when(transferService.getFileDetails(FILE_ID)).thenThrow(new FileDetailsException("unexpected file transfer status"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .build());

        //when
        final var submissions = Collections.singletonList(submission);
        final var exception = assertThrows(FileDetailsException.class,
            () -> decisionEngine.evaluateSubmissions(submissions)
        );
        assertThat(exception.getMessage(), is("unexpected file transfer status"));
    }

    private FileDetails getExpectedFileDetailsWaiting(final String fileId, final FileConversionStatus conversionStatus) {
        return FileDetails.builder()
                .withFileId(fileId)
                .withConversionStatus(conversionStatus)
                .build();
    }
}
