package uk.gov.companieshouse.efs.api.submissions.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmFormTypeControllerTest {

    @Mock
    private SubmissionResponseApi response;

    private ConfirmFormTypeController confirmFormTypeController;

    @Mock
    private FormTypeApi formType;

    @Mock
    private SubmissionService service;

    @Mock
    private Submission submission;

    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.confirmFormTypeController = new ConfirmFormTypeController(service);
    }

    @Test
    void testConfirmFormTypeReturnsSubmissionId() {
        //given
        when(service.updateSubmissionWithForm(any(), any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = confirmFormTypeController.confirmFormType("123", formType,
                result);

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }


    @Test
    void testConfirmFormTypeReturns409Conflict() {
        //given
        when(service.updateSubmissionWithForm(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = confirmFormTypeController.confirmFormType("123", formType, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testConfirmFormTypeReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "form.formType", "invalid"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = confirmFormTypeController.confirmFormType("123", formType,
                result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void testConfirmFormTypeReturns404NotFound() {
        // given
        when(service.updateSubmissionWithForm(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = confirmFormTypeController.confirmFormType("123", formType,
                result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
