package uk.gov.companieshouse.efs.api.paymentreports.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentTransactionTest {

    private PaymentTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new PaymentTransaction();
    }

    @Test
    void getSetSubmissionId() {
        testTransaction.setSubmissionId("id");

        assertThat(testTransaction.getSubmissionId(), is("id"));
    }

    @Test
    void getSetCustomerRef() {
        testTransaction.setCustomerRef("customerRef");

        assertThat(testTransaction.getCustomerRef(), is("customerRef"));
    }

    @Test
    void getSetUserEmail() {
        testTransaction.setUserEmail("userEmail");

        assertThat(testTransaction.getUserEmail(), is("userEmail"));
    }

    @Test
    void getSetSubmittedAt() {
        testTransaction.setSubmittedAt("submittedAt");

        assertThat(testTransaction.getSubmittedAt(), is("submittedAt"));
    }

    @Test
    void getSetAmountPaid() {
        testTransaction.setAmountPaid("amount");

        assertThat(testTransaction.getAmountPaid(), is("amount"));
    }

    @Test
    void getPaymentRef() {
        testTransaction.setPaymentRef("paymentRef");

        assertThat(testTransaction.getPaymentRef(), is("paymentRef"));
    }

    @Test
    void getFormType() {
        testTransaction.setFormType("form");

        assertThat(testTransaction.getFormType(), is("form"));
    }

    @Test
    void getCompanyNumber() {
        testTransaction.setCompanyNumber("company");

        assertThat(testTransaction.getCompanyNumber(), is("company"));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(PaymentTransaction.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void testToString() {
        assertThat(testTransaction.toString(),
            is("PaymentTransaction[submissionId=<null>,customerRef=<null>,userEmail=<null>,submittedAt=<null>"
                + ",amountPaid=<null>,paymentRef=<null>,formType=<null>,companyNumber=<null>]"));
    }
}