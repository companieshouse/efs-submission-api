package uk.gov.companieshouse.efs.api.fes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.fes.service.exception.ChipsServiceException;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
class FesServiceImplTest {

    private FesService fesService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private ChipsService chipsService;

    @Mock
    private EmailService emailService;

    @Mock
    private SubmissionRepository repository;

    @Mock
    private Submission submission;

    @Mock
    private FormDetails formDetails;

    private static final String BARCODE = "123";

    @BeforeEach
    public void setUp() {
        fesService = new FesServiceImpl(repository, emailService, chipsService, submissionService);
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeEmailAcceptedSuccess(){
        //given
        when(repository.readByBarcode(BARCODE)).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.SENT_TO_FES);

        //when
        fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.ACCEPTED);

        //then
        verify(repository).readByBarcode(BARCODE);
        verify(emailService).sendExternalAccept(any());
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeRejectedSuccess(){
        //given
        when(chipsService.getRejectReasons(any())).thenReturn(Collections.singletonList("test Reasons"));
        when(repository.readByBarcode(BARCODE)).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.SENT_TO_FES);

        //when
        fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.REJECTED);

        //then
        verify(repository).readByBarcode(BARCODE);
        verify(chipsService).getRejectReasons("123");
        verify(emailService).sendExternalReject(
                new ExternalRejectEmailModel(submission, Collections.singletonList("test Reasons")));
        verify(submissionService).updateSubmission(submission);
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeRejectedEmailException(){
        //given
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getBarcode()).thenReturn("123");
        when(chipsService.getRejectReasons(any())).thenReturn(Collections.singletonList("test Reasons"));
        when(repository.readByBarcode(BARCODE)).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.SENT_TO_FES);
        doThrow(new EmailServiceException("")).when(emailService).sendExternalReject(any());

        //when
        fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.REJECTED);

        //then
        verify(chipsService).getRejectReasons("123");
        verify(submission).setChipsRejectReasons(Collections.singletonList(new RejectReason("test Reasons")));
        verify(emailService).sendExternalReject(
                new ExternalRejectEmailModel(submission, Collections.singletonList("test Reasons")));
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeRejectedChipsException(){
        //given
        when(repository.readByBarcode(BARCODE)).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.SENT_TO_FES);
        doThrow(new ChipsServiceException("")).when(chipsService).getRejectReasons(any());

        //when
        fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.REJECTED);

        //then
        verify(chipsService).getRejectReasons("123");
        verify(submission).setChipsRejectReasons(Collections.emptyList());
        verify(emailService).sendExternalReject(new ExternalRejectEmailModel(submission, Collections.emptyList()));
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeInvalidSubmissionState(){
        //given
        when(repository.readByBarcode(BARCODE)).thenReturn(submission);
        when(submission.getStatus()).thenReturn(SubmissionStatus.OPEN);

        //when
        Executable actual = () -> fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.REJECTED);

        //then
        SubmissionIncorrectStateException ex =
                assertThrows(SubmissionIncorrectStateException.class, actual);
        assertEquals(String.format("Submission [%s] has invalid status: [%s]", null, SubmissionStatus.OPEN), ex.getMessage());
        verifyNoInteractions(emailService);
        verifyNoInteractions(chipsService);
        verifyNoInteractions(submissionService);
    }

    @Test
    void testUpdateSubmissionStatusByBarcodeNullSubmissionState(){
        //given
        when(repository.readByBarcode(BARCODE)).thenReturn(null);

        //when
        Executable actual = () -> fesService.updateSubmissionStatusByBarcode(BARCODE, FesSubmissionStatus.REJECTED);

        //then
        SubmissionNotFoundException ex =
                assertThrows(SubmissionNotFoundException.class, actual);
        assertEquals(String.format("Submission not found for barcode [%s]", BARCODE), ex.getMessage());
        verifyNoInteractions(emailService);
        verifyNoInteractions(chipsService);
        verifyNoInteractions(submissionService);
    }
}