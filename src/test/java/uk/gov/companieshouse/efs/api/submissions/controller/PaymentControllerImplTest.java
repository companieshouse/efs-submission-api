package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.payment.controller.PaymentController;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerImplTest {
    private static final Instant FIXED_NOW = Instant.parse("2020-03-15T09:44:08.108Z");
    private static final String SUB_ID = "4f56fdf78b357bfc";
    private static final long MILLIS_IN_SEC = 1000L;
    private static final String API_SERVICE_URL = "http://mock.api.service/app/";
    private static final String RESOURCE_URL = API_SERVICE_URL + SUB_ID + "/payment";
    private static final String PAYMENT_SESSION_ID = "12345678901234567890";
    private static final String PAYMENT_SESSION_STATE = "hjbrjgershgbearablgurealhrbeoa";

    private PaymentController testController;

    @Mock
    HttpServletRequest request;
    @Mock
    private PaymentTemplateService paymentService;
    @Mock
    private SubmissionService submissionService;
    @Mock
    private Logger logger;
    @Mock
    private BindingResult bindingResult;

    private Submission submission;
    private PaymentTemplate template;
    private SessionApi paymentSession;
    private SessionListApi paymentSessions;
    private URL paymentUrl;
    private ArgumentCaptor<Submission> subCaptor;

    @BeforeEach
    void setUp() throws MalformedURLException {
        testController = new PaymentController(submissionService, logger);
        paymentUrl = new URL(RESOURCE_URL);
        paymentSession = new SessionApi(PAYMENT_SESSION_ID, PAYMENT_SESSION_STATE);
        paymentSessions = new SessionListApi(Collections.singletonList(paymentSession));
        submission = Submission.builder().withPaymentSessions(paymentSessions).build();
        template = PaymentTemplate.newBuilder().build();
    }

/*
    @ParameterizedTest
    @MethodSource("provideValidTemplateArgs")
    void getPaymentDetailsWhenSecurityTypeValid(final SecurityType securityType, final String templateId) {
        application.setSecurityType(securityType);
        when(applicationService.getApplication(SUB_ID)).thenReturn(application);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);
        when(paymentService.getTemplate(templateId)).thenReturn(template);
        when(request.getRequestURL()).thenReturn(new StringBuffer(RESOURCE_URL));

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        checkResponse(response, templateId);

        final PaymentTemplate payment = response.getBody();

        assertThat(payment.getLinks().getResource(), is(API_SERVICE_URL + SUB_ID));
        assertThat(payment.getLinks().getSelf(), equalTo(paymentUrl));
    }

    @Test
    void getPaymentDetailsWhenSecurityTypeNull() {
        when(applicationService.getApplication(SUB_ID)).thenReturn(application);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void getPaymentDetailsWhenSecurityTypeInvalid() {
        application.setSecurityType(SecurityType.PSC);
        when(applicationService.getApplication(SUB_ID)).thenReturn(application);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ParameterizedTest
    @MethodSource("provideValidTemplateArgs")
    void getPaymentDetailsWhenRequestPathTrailingSlash(final SecurityType securityType, final String templateId) {
        application.setSecurityType(securityType);
        when(applicationService.getApplication(SUB_ID)).thenReturn(application);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);
        when(paymentService.getTemplate(templateId)).thenReturn(template);
        when(request.getRequestURL()).thenReturn(new StringBuffer(RESOURCE_URL + "/"));

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        checkResponse(response, templateId);

        final PaymentTemplate payment = response.getBody();

        assertThat(payment.getLinks().getResource(), is(API_SERVICE_URL + SUB_ID));
        assertThat(payment.getLinks().getSelf(), equalTo(paymentUrl));
    }

    @Test
    void getPaymentDetailsWhenBadId() {
        when(clock.instant()).thenReturn(FIXED_NOW);

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails("\t", request); // bad URI character

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getHeaders().getDate(), is(FIXED_NOW.getEpochSecond() * MILLIS_IN_SEC));
    }

    @Test
    void getPaymentDetailsWhenNoApplication() {
        when(applicationService.getApplication(SUB_ID)).thenReturn(null);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        verify(applicationService).getApplication(SUB_ID);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(response.getHeaders().getDate(), is(FIXED_NOW.getEpochSecond() * MILLIS_IN_SEC));
    }

    @ParameterizedTest
    @MethodSource("provideValidTemplateArgs")
    void getPaymentDetailsWhenNoTemplate(final SecurityType securityType, final String templateId) {
        application.setSecurityType(securityType);
        when(applicationService.getApplication(SUB_ID)).thenReturn(application);
        when(applicationService.getLatestOperationAt()).thenReturn(FIXED_NOW);
        when(paymentService.getTemplate(templateId)).thenReturn(null);

        ResponseEntity<PaymentTemplate> response = testController.getPaymentDetails(SUB_ID, request);

        verify(applicationService).getApplication(SUB_ID);
        verify(paymentService).getTemplate(templateId);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getHeaders().getDate(), is(FIXED_NOW.getEpochSecond() * MILLIS_IN_SEC));
    }

    private void checkResponse(ResponseEntity<PaymentTemplate> response, final String templateId) {
        verify(applicationService).getApplication(SUB_ID);
        verify(paymentService).getTemplate(templateId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getHeaders().getLocation().toString(), is("/app/" + SUB_ID));
        assertThat(response.getHeaders().getDate(), is(FIXED_NOW.getEpochSecond() * MILLIS_IN_SEC));
    }

    @Test
    void createPaymentSessionWhenIdIsFound() {
        final SecureApplication foundApplication = SecureApplication.newBuilder().withId(SUB_ID).withCreatedAt(
            FIRST_INSTANT).withPaymentSessions(new PaymentSessionList()).build();

        whenGetApplicationReturnFoundApplication(applicationService, foundApplication, request);

        final ResponseEntity<ApiErrors> response = testController.submitPaymentSessions(SUB_ID, paymentSessions,
            bindingResult, request);

        assertThat(foundApplication.getPaymentSessions(), is(paymentSessions));
    }
*/
    @Test
    void updateSubmissionWithPaymentSessions() {
        final SubmissionResponseApi response = new SubmissionResponseApi(SUB_ID);

        when(submissionService.updateSubmissionWithPaymentSessions(SUB_ID, paymentSessions)).thenReturn(response);

        final ResponseEntity<SubmissionResponseApi> actual =
            testController.submitPaymentSessions(SUB_ID, paymentSessions, bindingResult);

        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getId(), is(SUB_ID));
    }
}
