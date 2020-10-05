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
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentReferenceControllerTest {

    @Mock
    private PaymentReferenceApi paymentReference;

    private PaymentReferenceController paymentReferenceController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private Submission submission;

    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.paymentReferenceController = new PaymentReferenceController(service);
    }

    @Test
    void testSubmitPaymentReferenceReturnsId() {
        //given
        when(service.updateSubmissionWithPaymentReference(any(), any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = paymentReferenceController.submitPaymentReference("123",
                paymentReference, result);

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }


    @Test
    void testSubmitPaymentReferenceReturns409Conflict() {
        //given
        when(service.updateSubmissionWithPaymentReference(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = paymentReferenceController.submitPaymentReference("123",
                paymentReference, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testSubmitPaymentReferenceReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "payment_reference", "invalid"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = paymentReferenceController.submitPaymentReference("123",
                paymentReference, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    void testSubmitPaymentReferenceReturns404NotFound() {
        // given
        when(service.updateSubmissionWithPaymentReference(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = paymentReferenceController.submitPaymentReference("123",
                paymentReference, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
