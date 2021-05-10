package uk.gov.companieshouse.efs.api.payment.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.payment.PaymentClose;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionApiMapper;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionMapper;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    private static final String SUB_ID = "1234567890";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String PAYMENT_REQUEST_URL = "http://localhost:9999/efs-submission-api/submission";
    public static final String NOFORM = "NOFORM";
    public static final String NOCHARGE = "NOCHARGE";
    public static final String FORM = "FORM";
    public static final String CHARGED = "CHARGED";
    public static final String CHARGES = "CHARGES";
    public static final String UNKNOWN = "UNKNOWN";

    @Mock
    private SessionListApi paymentSessions;

    private PaymentController paymentController;

    @Mock
    private SubmissionService service;
    @Mock
    private FormTemplateService formTemplateService;
    @Mock
    private PaymentTemplateService paymentTemplateService;
    @Mock
    private SubmissionService submissionService;
    @Mock
    private EmailService emailService;
    @Mock
    private SubmissionApiMapper submissionApiMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SubmissionResponseApi response;

    @Mock
    private Logger logger;

    @Mock
    private BindingResult result;

    @Mock
    private PaymentClose paymentClose;

    private Company company;

    @BeforeEach
    void setUp() {
        this.paymentController =
            new PaymentController(service, formTemplateService, paymentTemplateService,
                submissionService, emailService, submissionApiMapper, logger);
        company = new Company(COMPANY_NUMBER, "test-company");
    }

    @Test
    void testGetPaymentDetails() {

        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final SubmissionApi submission = new SubmissionMapper()
            .map(new Submission.Builder().withFormDetails(formDetails).withCompany(company).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(CHARGED, "charged", null, CHARGES, false, false, null);
        final PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder().withId(CHARGED).build();

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(CHARGED)).thenReturn(formTemplate);
        when(paymentTemplateService.getTemplate(CHARGES)).thenReturn(Optional.of(paymentTemplate));
        when(request.getRequestURL())
            .thenReturn(new StringBuffer(PAYMENT_REQUEST_URL).append("/").append(SUB_ID).append("/"));

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        assertThat(paymentTemplate.getLinks().getSelf().toString(), is(PAYMENT_REQUEST_URL + "/" + SUB_ID));
        assertThat(paymentTemplate.getLinks().getResource(), is(PAYMENT_REQUEST_URL));
        assertThat(paymentTemplate.getCompanyNumber(), is(COMPANY_NUMBER));

    }

    @Test
    void testGetPaymentDetailsWhenSubmissionNotFound() {
        //given
        when(service.readSubmission(SUB_ID)).thenReturn(null);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void testGetPaymentDetailsWhenSubmissionFormNull() {
        //given
        SubmissionApi submission = new SubmissionMapper().map(new Submission.Builder().withId(SUB_ID).build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenSubmissionFormTypeNull() {
        //given
        final FormDetails formDetails = new FormDetails(NOFORM, null, null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenSubmissionFormTypeNotFound() {
        //given
        final FormDetails formDetails = new FormDetails("UNKNOWNFORM", UNKNOWN, null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(UNKNOWN)).thenReturn(null);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenSubmissionFormTypeBlank() {
        //given
        final FormDetails formDetails = new FormDetails(NOFORM, "", null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenPaymentChargeNull() {
        //given
        final FormDetails formDetails = new FormDetails(NOFORM, NOCHARGE, null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(NOCHARGE, "no charge", null, null, false, false, null);

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(NOCHARGE)).thenReturn(formTemplate);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenPaymentChargeBlank() {
        //given
        final FormDetails formDetails = new FormDetails(NOFORM, NOCHARGE, null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(NOCHARGE, "no charge", null, "", false, false, null);

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(NOCHARGE)).thenReturn(formTemplate);

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenPaymentChargeNotFound() {
        //given
        final FormDetails formDetails = new FormDetails(NOFORM, NOCHARGE, null);
        SubmissionApi submission =
            new SubmissionMapper().map(new Submission.Builder().withFormDetails(formDetails).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(NOCHARGE, "no charge", null, UNKNOWN, false, false, null);

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(NOCHARGE)).thenReturn(formTemplate);
        when(paymentTemplateService.getTemplate(UNKNOWN)).thenReturn(Optional.empty());

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetPaymentDetailsWhenRequestUrlBad() {
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        SubmissionApi submission = new SubmissionMapper()
            .map(new Submission.Builder().withFormDetails(formDetails).withCompany(company).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(CHARGED, "charged", null, CHARGES, false, false, null);
        final PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder().withId(CHARGED).build();

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(CHARGED)).thenReturn(formTemplate);
        when(paymentTemplateService.getTemplate(CHARGES)).thenReturn(Optional.of(paymentTemplate));
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("http://localhost:9999/efs-submission-api/submission{"));

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetPaymentDetailsWhenCompanyNull() {
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        SubmissionApi submission = new SubmissionMapper()
            .map(new Submission.Builder().withFormDetails(formDetails).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(CHARGED, "charged", null, CHARGES, false, false, null);
        final PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder().withId(CHARGED).build();

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(CHARGED)).thenReturn(formTemplate);
        when(paymentTemplateService.getTemplate(CHARGES)).thenReturn(Optional.of(paymentTemplate));

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

    }

    @Test
    void testGetPaymentDetailsWhenCompanyNumberBlank() {
        company.setCompanyNumber("");

        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        SubmissionApi submission = new SubmissionMapper()
            .map(new Submission.Builder().withFormDetails(formDetails).withCompany(company).build());
        final FormTemplateApi formTemplate = new FormTemplateApi(CHARGED, "charged", null, CHARGES, false, false, null);
        final PaymentTemplate paymentTemplate = PaymentTemplate.newBuilder().withId(CHARGED).build();

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(formTemplateService.getFormTemplate(CHARGED)).thenReturn(formTemplate);
        when(paymentTemplateService.getTemplate(CHARGES)).thenReturn(Optional.of(paymentTemplate));

        //when
        final ResponseEntity<PaymentTemplate> response = paymentController.getPaymentDetails(SUB_ID, request);

        //then
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

    }

    @Test
    void testSubmitPaymentSessionsReturnsId() {
        //given
        paymentSessions = new SessionListApi();
        when(service.updateSubmissionWithPaymentSessions("123", paymentSessions)).thenReturn(response);

        //when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.submitPaymentSessions("123", paymentSessions, result);

        //then
        assertThat(actual.getBody(), is(equalTo(response)));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }


    @Test
    void testSubmitPaymentSessionsReturns409Conflict() {
        //given
        when(service.updateSubmissionWithPaymentSessions(SUB_ID, paymentSessions))
            .thenThrow(new SubmissionIncorrectStateException("not OPEN"));

        //when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.submitPaymentSessions(SUB_ID, paymentSessions, result);

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
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.submitPaymentSessions("123", paymentSessions, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    void testSubmitPaymentSessionsReturns404NotFound() {
        // given
        when(service.updateSubmissionWithPaymentSessions(SUB_ID, paymentSessions))
            .thenThrow(new SubmissionNotFoundException("not found"));

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.submitPaymentSessions(SUB_ID, paymentSessions, result);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns404NotFound() {
        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns409Conflict() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final SubmissionApi submission = new SubmissionMapper().map(
            new Submission.Builder().withFormDetails(formDetails)
                .withCompany(company)
                .build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        submission.setStatus(SubmissionStatus.SUBMITTED); // invalid status

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns400BadRequestWhenSessionNotFound() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final SubmissionApi submission = new SubmissionMapper().map(
            new Submission.Builder().withFormDetails(formDetails)
                .withCompany(company)
                .build());

        when(service.readSubmission(SUB_ID)).thenReturn(submission);
        when(submissionService.updateSubmissionWithPaymentOutcome(SUB_ID, paymentClose)).thenThrow(
            new SubmissionIncorrectStateException("test exception"));
        submission.setStatus(SubmissionStatus.PAYMENT_REQUIRED);
        // submission has no matching payment session details

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }
    
    @Test
    void closePaymentSessionReturns204NoContentWhenPaidBeforeCompletion() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final Submission submission = new Submission.Builder().withFormDetails(formDetails)
            .withCompany(company)
            .withStatus(SubmissionStatus.OPEN)
            .build();
        final SubmissionApi submissionApi = new SubmissionMapper().map(submission);
        final SubmissionResponseApi submissionResponse = new SubmissionResponseApi(SUB_ID);

        when(service.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(submissionService.updateSubmissionWithPaymentOutcome(SUB_ID, paymentClose)).thenReturn(submissionResponse);

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        verifyNoInteractions(emailService);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns204NoContentWhenFailedAfterCompletion() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final Submission submission = new Submission.Builder().withFormDetails(formDetails)
            .withCompany(company)
            .withStatus(SubmissionStatus.PAYMENT_REQUIRED)
            .build();
        final SubmissionApi submissionApi = new SubmissionMapper().map(submission);
        final SubmissionResponseApi submissionResponse = new SubmissionResponseApi(SUB_ID);

        when(service.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(paymentClose.isPaid()).thenReturn(false);
        when(submissionService.updateSubmissionWithPaymentOutcome(SUB_ID, paymentClose)).thenReturn(submissionResponse);

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        verify(emailService, never()).sendExternalConfirmation(new ExternalConfirmationEmailModel(submission));
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns204NoContentWhenFailedBeforeCompletion() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final Submission submission = new Submission.Builder().withFormDetails(formDetails)
            .withCompany(company)
            .withStatus(SubmissionStatus.OPEN)
            .build();
        final SubmissionApi submissionApi = new SubmissionMapper().map(submission);
        final SubmissionResponseApi submissionResponse = new SubmissionResponseApi(SUB_ID);

        when(service.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(submissionService.updateSubmissionWithPaymentOutcome(SUB_ID, paymentClose)).thenReturn(submissionResponse);

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        verifyNoInteractions(emailService);
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void closePaymentSessionReturns204NoContentWhenPaidAfterCompletion() {
        // given
        final FormDetails formDetails = new FormDetails(FORM, CHARGED, null);
        final Submission submission = new Submission.Builder().withFormDetails(formDetails)
            .withCompany(company)
            .withStatus(SubmissionStatus.PAYMENT_REQUIRED)
            .build();
        final SubmissionApi submissionApi = new SubmissionMapper().map(submission);
        final SubmissionResponseApi submissionResponse = new SubmissionResponseApi(SUB_ID);

        when(service.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(paymentClose.isPaid()).thenReturn(true);
        when(submissionService.updateSubmissionWithPaymentOutcome(SUB_ID, paymentClose)).thenReturn(submissionResponse);
        when(submissionApiMapper.map(submissionApi)).thenReturn(submission);

        // when
        ResponseEntity<SubmissionResponseApi> actual =
            paymentController.patchPaymentSession(SUB_ID, paymentClose, request);

        // then
        verify(emailService).sendExternalConfirmation(new ExternalConfirmationEmailModel(submission));
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

}
