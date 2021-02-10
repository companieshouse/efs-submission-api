package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ConfirmAuthorisedControllerTest {

    private ConfirmAuthorisedController confirmAuthorisedController;
    @Mock
    private ConfirmAuthorisedApi confirmAuthorised;
    @Mock
    private SubmissionService service;
    @Mock
    private SubmissionResponseApi response;
    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.confirmAuthorisedController = new ConfirmAuthorisedController(service);
    }

    @Test
    void submitAuthorisedReturnsId() {
        //given
        when(service.updateSubmissionConfirmAuthorised(any(), any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = confirmAuthorisedController.submitAuthorised(
            "123", confirmAuthorised, result);

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void testSubmitAuthorisedReturns409Conflict() {
        //given
        when(service.updateSubmissionConfirmAuthorised(any(), any())).thenThrow(
            new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = confirmAuthorisedController.submitAuthorised(
            "123", confirmAuthorised, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testSubmitAuthorisedReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError(
            "a", "confirm_authorised", "invalid"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = confirmAuthorisedController.submitAuthorised(
            "123", confirmAuthorised, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testSubmitAuthorisedReturns404NotFound() {
        // given
        when(service.updateSubmissionConfirmAuthorised(any(), any())).thenThrow(
            new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = confirmAuthorisedController.submitAuthorised(
            "123", confirmAuthorised, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}