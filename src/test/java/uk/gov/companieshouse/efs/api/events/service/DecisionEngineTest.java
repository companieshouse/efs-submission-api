package uk.gov.companieshouse.efs.api.events.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.filetransfer.FileTransferApiClient;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    private DecisionEngine decisionEngine;

    @Mock
    private FileTransferApiClient apiClient;

    @Mock
    private Submission submission;

    @Mock
    private FormTemplateRepository formTemplateRepository;

    @Mock
    private CurrentTimestampGenerator timestampGenerator;

    @Mock
    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        this.decisionEngine = new DecisionEngine(apiClient, formTemplateRepository, timestampGenerator, submissionService);
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfFileUnscanned() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.OK, "waiting"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .build());

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.NO_DECISION).size());
        verify(apiClient).details("abc");
    }

    @Test
    void testDecisionEngineReturnsNotCleanIfAllFilesScannedOneFileNotClean() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        LocalDateTime now = LocalDateTime.now();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.OK, "infected"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .build());
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.NOT_CLEAN).size());
        assertEquals(now, fileDetails.getLastModifiedAt());
        assertEquals(FileConversionStatus.FAILED_AV, fileDetails.getConversionStatus());
        verify(timestampGenerator).generateTimestamp();
        verify(apiClient).details("abc");
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfOneFileNotCleanOtherFilesUnscanned() {
        //given
        when(apiClient.details(anyString()))
                .thenReturn(new FileTransferApiClientDetailsResponse("abd", HttpStatus.OK, "waiting"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Arrays.asList(FileDetails.builder()
                        .withFileId("abc")
                        .withConversionStatus(FileConversionStatus.FAILED_AV)
                        .build(), FileDetails.builder()
                        .withFileId("abd")
                        .withConversionStatus(FileConversionStatus.WAITING)
                        .build()))
                .build());

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.NO_DECISION).size());
        verify(apiClient, times(1)).details(anyString());
    }

    @Test
    void testDecisionEngineReturnsFesEnabledIfFormTypeEnabledInFes() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        LocalDateTime now = LocalDateTime.now();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.OK, "clean"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .withFormType("AD01")
                .build());
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(anyString())).thenReturn(Optional.of(
            FormTemplate.builder()
                .withFormType("AD01")
                .withFormName("Change of address")
                .withFormCategory("CC")
                .withFesEnabled(true)
                .withFesDocType("FES")
                .build()));

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.FES_ENABLED).size());
        assertEquals(now, fileDetails.getLastModifiedAt());
        assertEquals(FileConversionStatus.CLEAN_AV, fileDetails.getConversionStatus());
        verify(timestampGenerator).generateTimestamp();
        verify(apiClient).details("abc");
        verify(submissionService).updateSubmission(submission);
        verify(formTemplateRepository).findById("AD01");
    }

    @Test
    void testDecisionEngineReturnsNotFesEnabledIfFormTypeDisabledInFes() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        LocalDateTime now = LocalDateTime.now();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.OK, "clean"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .withFormType("AD01")
                .build());
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(anyString())).thenReturn(Optional.of(
            FormTemplate.builder()
                .withFormType("AD01")
                .withFormName("Change of address")
                .withFormCategory("CC")
                .withFesDocType("FES")
                .build()));

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.NOT_FES_ENABLED).size());
        assertEquals(now, fileDetails.getLastModifiedAt());
        assertEquals(FileConversionStatus.CLEAN_AV, fileDetails.getConversionStatus());
        verify(timestampGenerator).generateTimestamp();
        verify(apiClient).details("abc");
        verify(submissionService).updateSubmission(submission);
        verify(formTemplateRepository).findById("AD01");
    }

    @Test
    void testDecisionEngineReturnsFormTypeDoesNotExistIfFormUnhandled() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        LocalDateTime now = LocalDateTime.now();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.OK, "clean"));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .withFormType("AD01")
                .build());
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(formTemplateRepository.findById(anyString())).thenReturn(Optional.empty());

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.FORM_TYPE_DOES_NOT_EXIST).size());
        assertEquals(now, fileDetails.getLastModifiedAt());
        assertEquals(FileConversionStatus.CLEAN_AV, fileDetails.getConversionStatus());
        verify(timestampGenerator).generateTimestamp();
        verify(apiClient).details("abc");
        verify(submissionService).updateSubmission(submission);
        verify(formTemplateRepository).findById("AD01");
    }

    @Test
    void testDecisionEngineReturnsNoDecisionIfFileTransferApiReturnsNon200() {
        //given
        FileDetails fileDetails = getExpectedFileDetailsWaiting();
        when(apiClient.details(anyString())).thenReturn(new FileTransferApiClientDetailsResponse("abc", HttpStatus.NOT_FOUND, null));
        when(submission.getFormDetails()).thenReturn(FormDetails.builder()
                .withFileDetailsList(Collections.singletonList(fileDetails))
                .build());

        //when
        Map<DecisionResult, List<Decision>> actual = decisionEngine
                .evaluateSubmissions(Collections.singletonList(submission));

        //then
        assertEquals(1, actual.get(DecisionResult.NO_DECISION).size());
        verify(apiClient).details("abc");
    }

    private FileDetails getExpectedFileDetailsWaiting() {
        return FileDetails.builder()
                .withFileId("abc")
                .withConversionStatus(FileConversionStatus.WAITING)
                .build();
    }
}
