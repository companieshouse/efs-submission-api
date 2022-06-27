package uk.gov.companieshouse.efs.api.events.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.events.service.model.DecisionResult;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionEngineTest {

    private static final String BUCKET_NAME = "TEST_BUCKET";

    private ExecutionEngine executionEngine;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private MessageService messageService;

    @Mock
    private EmailService emailService;

    @Mock
    private S3ClientService s3ClientService;

    @Mock
    private Decision decision;

    @Mock
    private Submission submission;

    @Mock
    private FormDetails formDetails;

    @Mock
    private FileDetails fileDetails;

    @BeforeEach
    void setUp() {
        this.executionEngine = new ExecutionEngine(submissionService, messageService, emailService, s3ClientService, BUCKET_NAME);
    }

    @Test
    void testExecutionEngineHandlesNoDecision() {
        //given
        when(decision.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");

        //when
        this.executionEngine.execute(Collections.singletonMap(DecisionResult.NO_DECISION, Collections.singletonList(decision)));

        //then
        verifyNoInteractions(submissionService);
        verifyNoInteractions(messageService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(s3ClientService);
    }

    @Test
    void testExecutionEngineHandlesInvalidFormType() {
        //given
        when(decision.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("IN01");

        //when
        this.executionEngine.execute(Collections.singletonMap(DecisionResult.FORM_TYPE_DOES_NOT_EXIST, Collections.singletonList(decision)));

        //then
        verifyNoInteractions(submissionService);
        verifyNoInteractions(messageService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(s3ClientService);
    }

    @Test
    void testExecutionEngineHandlesInfectedFiles() {
        //given
        when(decision.getSubmission()).thenReturn(submission);
        when(decision.getInfectedFiles()).thenReturn(Collections.singletonList("infected.pdf"));
        when(submission.getId()).thenReturn("123");

        //when
        executionEngine.execute(Collections.singletonMap(DecisionResult.NOT_CLEAN, Collections.singletonList(decision)));

        //then
        verify(submissionService).updateSubmissionStatus("123", SubmissionStatus.REJECTED_BY_VIRUS_SCAN);
        verify(emailService).sendInternalFailedAV(new InternalAvFailedEmailModel(submission, Collections.singletonList("infected.pdf")));
        verifyNoInteractions(messageService);
        verifyNoInteractions(s3ClientService);
    }

    @Test
    void testExecutionEngineHandlesFesEnabledForms() {
        // given
        when(decision.getSubmission()).thenReturn(submission);
        List<Decision> expectedDecisions = Collections.singletonList(decision);

        // when
        executionEngine.execute(Collections.singletonMap(DecisionResult.FES_ENABLED, expectedDecisions));

        // then
        verify(submissionService).updateSubmissionQueued(submission);
        verify(messageService).queueMessages(expectedDecisions);
        verifyNoInteractions(emailService);
        verifyNoInteractions(s3ClientService);
    }

    @Test
    void testExecutionEngineHandlesNonFesEnabledForm() {
        // given
        when(decision.getSubmission()).thenReturn(submission);
        List<Decision> expectedDecisions = Collections.singletonList(decision);
        when(submission.getId()).thenReturn("123");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));
        when(fileDetails.getFileId()).thenReturn("abc123");
        when(s3ClientService.generateFileLink(anyString(), anyString())).thenReturn("http://chs-dev.internal:4001");

        // when
        executionEngine.execute(Collections.singletonMap(DecisionResult.NOT_FES_ENABLED, expectedDecisions));

        // then
        verify(submissionService).updateSubmissionStatus("123", SubmissionStatus.PROCESSED_BY_EMAIL);
        verify(emailService).sendInternalSubmission(new InternalSubmissionEmailModel(submission, Collections.singletonList(new EmailFileDetails(fileDetails, "http://chs-dev.internal:4001"))));
        verify(s3ClientService).generateFileLink("abc123", BUCKET_NAME);
        verifyNoInteractions(messageService);
    }
}
