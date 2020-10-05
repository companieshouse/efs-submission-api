package uk.gov.companieshouse.efs.api.paymentreports.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentTransactionBuilderTest {
    private static final Instant FIXED_NOW = Instant.parse("2020-08-21T10:48:44Z");


    private PaymentTransactionBuilder paymentTransactionBuilder;

    private PaymentTransaction expected;

    @BeforeEach
    void setUp() {
        paymentTransactionBuilder = new PaymentTransactionBuilder();
        expected = new PaymentTransaction();
    }

    @Test
    void testWithSubmissionId() {
        expected.setSubmissionId("id");

        assertThat(paymentTransactionBuilder.withSubmissionId("id").build(), is(equalTo(expected)));
    }

    @Test
    void testWithCustomerRef() {
        expected.setCustomerRef("customerRef");

        assertThat(paymentTransactionBuilder.withCustomerRef("customerRef").build(), is(equalTo(expected)));
    }

    @Test
    void testWithUserEmail() {
        expected.setUserEmail("userEmail");

        assertThat(paymentTransactionBuilder.withUserEmail("userEmail").build(), is(equalTo(expected)));
    }

    @Test
    void testWithSubmittedAt() {
        final LocalDateTime submitted = LocalDateTime.ofInstant(FIXED_NOW, ZoneOffset.UTC);

        expected.setSubmittedAt(submitted.toString());

        assertThat(paymentTransactionBuilder.withSubmittedAt(submitted).build(), is(equalTo(expected)));
    }

    @Test
    void testWithAmountPaid() {
        expected.setAmountPaid("amount");

        assertThat(paymentTransactionBuilder.withAmountPaid("amount").build(), is(equalTo(expected)));
    }

    @Test
    void testWithPaymentRef() {
        expected.setPaymentRef("paymentRef");

        assertThat(paymentTransactionBuilder.withPaymentRef("paymentRef").build(), is(equalTo(expected)));
    }

    @Test
    void testWithFormType() {
        expected.setFormType("form");

        assertThat(paymentTransactionBuilder.withFormType("form").build(), is(equalTo(expected)));
    }

    @Test
    void testWithCompanyNumber() {
        expected.setCompanyNumber("company");

        assertThat(paymentTransactionBuilder.withCompanyNumber("company").build(), is(equalTo(expected)));
    }

}