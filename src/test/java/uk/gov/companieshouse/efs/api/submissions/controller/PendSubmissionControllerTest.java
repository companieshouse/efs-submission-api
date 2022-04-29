package uk.gov.companieshouse.efs.api.submissions.controller;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendSubmissionControllerTest {

    public static final String SUBMISSION_ID = "123";
    private PendStatusController pendStatusController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionApi submissionApi;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        this.pendStatusController = new PendStatusController(service);
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionStatus.class, names = {"OPEN", "PAYMENT_REQUIRED"})
    void testPendSubmission(SubmissionStatus status) {
        //given
        when(service.readSubmission(any())).thenReturn(submissionApi);
        when(submissionApi.getStatus()).thenReturn(status);

        when(response.getId()).thenReturn(SUBMISSION_ID);
        when(service.updateSubmissionStatus(SUBMISSION_ID, SubmissionStatus.PAYMENT_REQUIRED)).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = pendStatusController.submitPendingPaymentStatus(SUBMISSION_ID, request);

        //then
        assertEquals(SUBMISSION_ID, actual.getBody().getId());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        verify(service).updateSubmissionStatus(SUBMISSION_ID, SubmissionStatus.PAYMENT_REQUIRED);
    }

    @Test
    void testReturnConflictIfSubmissionStatusNotPendable() throws SubmissionValidationException {

        //given
        when(service.readSubmission(any())).thenReturn(submissionApi);
        when(submissionApi.getStatus()).thenReturn(SubmissionStatus.SUBMITTED);

        //when
        ResponseEntity<SubmissionResponseApi> actual = pendStatusController.submitPendingPaymentStatus(SUBMISSION_ID, request);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testReturnNotFoundIfSubmissionDoesNotExist() throws SubmissionValidationException {
        //given
        when(service.readSubmission(any())).thenReturn(null);

        //when
        ResponseEntity<SubmissionResponseApi> actual = pendStatusController.submitPendingPaymentStatus(SUBMISSION_ID, request);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
