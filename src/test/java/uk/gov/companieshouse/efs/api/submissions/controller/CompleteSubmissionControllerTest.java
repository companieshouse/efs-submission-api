package uk.gov.companieshouse.efs.api.submissions.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteSubmissionControllerTest {

    private CompleteSubmissionController completeSubmissionController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionResponseApi response;


    @BeforeEach
    void setUp() {
        this.completeSubmissionController = new CompleteSubmissionController(service);
    }

    @Test
    void testCompleteSubmission() throws SubmissionValidationException {
        //given
        when(service.completeSubmission(any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = completeSubmissionController.completeSubmission("123");

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void testReturnConflictIfSubmissionStatusNotOpen() throws SubmissionValidationException {
        //given
        when(service.completeSubmission(any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = completeSubmissionController.completeSubmission("123");

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testValidationReturnsBadRequest() throws SubmissionValidationException {
        //given
        when(service.completeSubmission(any())).thenThrow(new SubmissionValidationException("dodgy data"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = completeSubmissionController.completeSubmission("123");

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testReturnNotFoundIfSubmissionDoesNotExist() throws SubmissionValidationException {
        //given
        when(service.completeSubmission(any())).thenThrow(new SubmissionNotFoundException("not found"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = completeSubmissionController.completeSubmission("123");

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
