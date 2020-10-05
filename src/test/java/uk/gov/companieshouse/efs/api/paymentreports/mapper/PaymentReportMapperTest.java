package uk.gov.companieshouse.efs.api.paymentreports.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

class PaymentReportMapperTest {
    private static final Instant FIXED_NOW = Instant.parse("2020-08-21T10:48:44Z");

    PaymentReportMapper testMapper;

    @BeforeEach
    void setUp() {
        testMapper = new PaymentReportMapper();
    }

    @Test
    void map() {
        Presenter presenter = new Presenter("email");
        FormDetails formDetails = new FormDetails(null, "formType", null);
        Company company = new Company("companyNumber", "companyName");
        final LocalDateTime submitted = LocalDateTime.ofInstant(FIXED_NOW, ZoneOffset.UTC);
        Submission submission =
            Submission.builder().withId("id").withConfirmationReference("confirmRef").withPresenter(presenter)
                .withSubmittedAt(submitted).withFeeOnSubmission("fee")
                .withPaymentReference("paymentRef").withFormDetails(formDetails).withCompany(company).build();

        final PaymentTransaction paymentTransaction = testMapper.map(submission);

        assertThat(paymentTransaction.getSubmissionId(), is("id"));
        assertThat(paymentTransaction.getCustomerRef(), is("confirmRef"));
        assertThat(paymentTransaction.getUserEmail(), is("email"));
        assertThat(paymentTransaction.getSubmittedAt(), is(submitted.toString()));
        assertThat(paymentTransaction.getAmountPaid(), is("fee"));
        assertThat(paymentTransaction.getPaymentRef(), is("paymentRef"));
        assertThat(paymentTransaction.getFormType(), is(formDetails.getFormType()));
        assertThat(paymentTransaction.getCompanyNumber(), is(company.getCompanyNumber()));

    }
}