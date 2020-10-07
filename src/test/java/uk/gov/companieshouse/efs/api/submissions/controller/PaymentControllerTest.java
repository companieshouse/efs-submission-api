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
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.payment.controller.PaymentController;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private SessionListApi paymentSessions;

    private PaymentController paymentController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private Submission submission;

    @Mock
    private Logger logger;

    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        this.paymentController = new PaymentController(service, logger);
    }

    @Test
    void testSubmitPaymentSessionsReturnsId() {
        //given
        paymentSessions = new SessionListApi();
        when(service.updateSubmissionWithPaymentSessions("123", paymentSessions)).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual = paymentController.submitPaymentSessions("123",
            paymentSessions, result);

        //then
        assertThat(actual.getBody(), is(equalTo(response)));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }


    @Test
    void testSubmitPaymentSessionsReturns409Conflict() {
        //given
        when(service.updateSubmissionWithPaymentSessions(any(), any())).thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual = paymentController.submitPaymentSessions("123",
            paymentSessions, result);

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void testSubmitPaymentSessionsReturns400BadRequest() {
        // given
        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(new FieldError("a", "payment_reference", "invalid"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = paymentController.submitPaymentSessions("123",
            paymentSessions, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    void testSubmitPaymentSessionsReturns404NotFound() {
        // given
        when(service.updateSubmissionWithPaymentSessions(any(), any())).thenThrow(new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual = paymentController.submitPaymentSessions("123",
            paymentSessions, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
