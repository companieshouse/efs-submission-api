package uk.gov.companieshouse.efs.api.paymentreports.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

class PaymentReportMapperTest {
    private static final Instant FIXED_NOW = Instant.parse("2020-08-21T10:48:44Z");

    PaymentReportMapper testMapper;
    private Presenter presenter;
    private FormDetails formDetails;
    private Company company;
    private LocalDateTime submitted;
    private SessionListApi paySessions;
    private Submission submission;

    @BeforeEach
    void setUp() {
        testMapper = new PaymentReportMapper();
        presenter = new Presenter("email");
        formDetails = new FormDetails(null, "formType", null);
        company = new Company("companyNumber", "companyName");
        submitted = LocalDateTime.ofInstant(FIXED_NOW, ZoneOffset.UTC);
        paySessions = new SessionListApi();
    }

    @Test
    void map() {
        paySessions = new SessionListApi(IntStream.range(1, 10)
            .mapToObj(
                i -> new SessionApi("test-pay-ref-" + i, "test-state-" + i, "test-status-" + i))
            .collect(Collectors.toList()));
        submission = Submission.builder().withId("id").withConfirmationReference("confirmRef").withPresenter(presenter)
            .withSubmittedAt(submitted).withFeeOnSubmission("fee").withPaymentSessions(paySessions)
            .withFormDetails(formDetails).withCompany(company).build();

        final PaymentTransaction paymentTransaction = testMapper.map(submission);

        assertThat(paymentTransaction.getSubmissionId(), is("id"));
        assertThat(paymentTransaction.getCustomerRef(), is("confirmRef"));
        assertThat(paymentTransaction.getUserEmail(), is("email"));
        assertThat(paymentTransaction.getSubmittedAt(), is(submitted.toString()));
        assertThat(paymentTransaction.getAmountPaid(), is("fee"));
        assertThat(paymentTransaction.getPaymentRef(), is("test-pay-ref-9"));
        assertThat(paymentTransaction.getFormType(), is(formDetails.getFormType()));
        assertThat(paymentTransaction.getCompanyNumber(), is(company.getCompanyNumber()));

    }

    @Test
    void mapWhenNoPaymentSessions() {
        submission = Submission.builder().withId("id").withConfirmationReference("confirmRef").withPresenter(presenter)
            .withSubmittedAt(submitted).withFeeOnSubmission("fee").withPaymentSessions(paySessions)
            .withFormDetails(formDetails).withCompany(company).build();

        final PaymentTransaction paymentTransaction = testMapper.map(submission);

        assertThat(paymentTransaction.getSubmissionId(), is("id"));
        assertThat(paymentTransaction.getCustomerRef(), is("confirmRef"));
        assertThat(paymentTransaction.getUserEmail(), is("email"));
        assertThat(paymentTransaction.getSubmittedAt(), is(submitted.toString()));
        assertThat(paymentTransaction.getAmountPaid(), is("fee"));
        assertThat(paymentTransaction.getPaymentRef(), is(nullValue()));
        assertThat(paymentTransaction.getFormType(), is(formDetails.getFormType()));
        assertThat(paymentTransaction.getCompanyNumber(), is(company.getCompanyNumber()));

    }
}