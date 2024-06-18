package uk.gov.companieshouse.efs.api.payment.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.efs.api.submissions.mapper.SubmissionApiMapper;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.logging.Logger;

@SpringBootTest(classes = PaymentController.class)
@Import(PaymentControllerIT.ClockConfig.class)
@AutoConfigureMockMvc
@AutoConfigureWebMvc
class PaymentControllerIT {
    private static final LocalDateTime FIXED_NOW = LocalDateTime.parse("2019-01-08T00:00");
    private static final String FORM_TEMPLATE = "FORM_BEARING_FEE";
    private static final String FEE_TEMPLATE = "FEE_TEMPLATE";
    private static final String AMOUNT_OUTGOING = "10";
    private static final String AMOUNT_INCOMING = "20";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String USER = "user";
    private static final String KEY = "key";
    private static final String SUB_ID = "1234567890";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String PAYMENT_URL_TEMPLATE =
            "/efs-submission-api/submission/{id}/payment";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserAuthenticationInterceptor userAuthenticationInterceptor;
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private FormTemplateService formTemplateService;
    @MockBean
    private PaymentTemplateService paymentTemplateService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private SubmissionApiMapper submissionApiMapper;
    @MockBean
    private Logger logger;
    private HttpHeaders httpHeaders;
    private CompanyApi companyApi;
    private SubmissionApi submissionApi;
    private SubmissionFormApi submissionFormApi;
    private FormTemplateApi formTemplateApi;
    private PaymentTemplate.Item item;
    private PaymentTemplate.Links links;
    private PaymentTemplateId paymentTemplateId;
    private PaymentTemplate paymentTemplate;

    @TestConfiguration
    static class ClockConfig {

        @Bean
        public Clock getClock() {
            return Clock.fixed(FIXED_NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        }

    }

    @BeforeEach
    void setUp() throws MalformedURLException, URISyntaxException {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);
        httpHeaders.add("ERIC-Identity", USER);
        httpHeaders.add("ERIC-Identity-Type", KEY);

        submissionFormApi = new SubmissionFormApi();
        submissionFormApi.setFormType(FORM_TEMPLATE);

        companyApi = new CompanyApi();
        companyApi.setCompanyNumber(COMPANY_NUMBER);
        companyApi.setCompanyName("ConHugeCo Inc.");

        submissionApi = new SubmissionApi();
        submissionApi.setId(SUB_ID);
        submissionApi.setCompany(companyApi);
        submissionApi.setSubmissionForm(submissionFormApi);

        formTemplateApi = new FormTemplateApi();
        formTemplateApi.setFormType(FORM_TEMPLATE);
        formTemplateApi.setPaymentCharge(FEE_TEMPLATE);

        item = PaymentTemplate.Item.newBuilder()
            .withAmount("100")
            .withAvailablePaymentMethods(Collections.singletonList("credit-card"))
            .withClassOfPayment(Collections.singletonList("data-maintenance"))
            .withDescription("Upload a form to Companies House")
            .withDescriptionId("AMOUNT_TO_PAY")
            .withKind("cost#cost")
            .withProductType("efs-test")
            .build();
        links = new PaymentTemplate.Links("http://resource.url", new URI("http://self.url").toURL());
        paymentTemplateId = new PaymentTemplateId(FEE_TEMPLATE, FIXED_NOW);
        paymentTemplate = PaymentTemplate.newBuilder()
            .withId(paymentTemplateId)
            .withDescription("Upload a form to Companies house")
            .withEtag("d8a936fc59fd43ba6c66363c25684be1964ea03d")
            .withItem(item)
            .withKind("cost#cost")
            .withLinks(links)
            .withPaymentReference("Test Charge")
            .withStatus(PaymentTemplate.Status.PENDING)
            .withCompanyNumber(COMPANY_NUMBER)
            .build();
    }

    @Test
    void getPaymentDetailsWhenTemplateNotFoundThen500() throws Exception {

        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(formTemplateApi);
        when(paymentTemplateService.getPaymentTemplate(FEE_TEMPLATE, FIXED_NOW)).thenReturn(
                Optional.empty());

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenTemplateFoundThen200() throws Exception {
        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(formTemplateApi);
        when(paymentTemplateService.getPaymentTemplate(FEE_TEMPLATE, FIXED_NOW)).thenReturn(
            Optional.of(paymentTemplate));

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").hasJsonPath())
            .andExpect(jsonPath("$.id.fee").value(FEE_TEMPLATE))
            .andExpect(jsonPath("$.id.active_from").value("2019-01-08T00:00:00"))
            .andExpect(jsonPath("$.items[0].amount").value("100"));

    }

    @Test
    void getPaymentDetailsWhenSubmissionApiNullThen404() throws Exception {

        when(submissionService.readSubmission(SUB_ID)).thenReturn(null);

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenSubmissionFormTemplateNullThen500() throws Exception {

        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(null);

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenSubmissionFormBlankThen500() throws Exception {

        SubmissionFormApi submissionFormApiBlank = new SubmissionFormApi();

        SubmissionApi submissionApi = new SubmissionApi();
        submissionApi.setId(SUB_ID);
        submissionApi.setCompany(companyApi);
        submissionApi.setSubmissionForm(submissionFormApiBlank);

        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApi);

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenSubmissionFormNullThen500() throws Exception {

        SubmissionApi submissionApiFormNull = new SubmissionApi();
        submissionApi.setId(SUB_ID);
        submissionApi.setCompany(companyApi);

        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApiFormNull);

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenPaymentChargeBlankThen500() throws Exception {

        FormTemplateApi formTemplateApiBlankCharge = new FormTemplateApi();
        formTemplateApiBlankCharge.setFormType(FORM_TEMPLATE);
        formTemplateApiBlankCharge.setPaymentCharge("");

        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(
                formTemplateApiBlankCharge);
        when(paymentTemplateService.getPaymentTemplate(FEE_TEMPLATE, FIXED_NOW)).thenReturn(
                Optional.of(paymentTemplate));

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenCompanyNullThen500() throws Exception {

        SubmissionApi submissionApiCompanyNull = new SubmissionApi();
        submissionApiCompanyNull.setId(SUB_ID);
        submissionApiCompanyNull.setSubmissionForm(submissionFormApi);

        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(formTemplateApi);
        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApiCompanyNull);
        when(paymentTemplateService.getPaymentTemplate(FEE_TEMPLATE, FIXED_NOW)).thenReturn(
                Optional.of(paymentTemplate));

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getPaymentDetailsWhenCompanyNumberBlankThen500() throws Exception {

        CompanyApi companyApiBlankCompNo = new CompanyApi();

        SubmissionApi submissionApiBlankCompNo = new SubmissionApi();
        submissionApiBlankCompNo.setId(SUB_ID);
        submissionApiBlankCompNo.setCompany(companyApiBlankCompNo);
        submissionApiBlankCompNo.setSubmissionForm(submissionFormApi);

        when(formTemplateService.getFormTemplate(FORM_TEMPLATE)).thenReturn(formTemplateApi);
        when(submissionService.readSubmission(SUB_ID)).thenReturn(submissionApiBlankCompNo);

        mockMvc.perform(get(PAYMENT_URL_TEMPLATE, SUB_ID).headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$").doesNotExist());

    }

}
