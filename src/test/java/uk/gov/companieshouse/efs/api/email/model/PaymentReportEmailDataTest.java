package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentReportEmailDataTest {
    PaymentReportEmailData paymentReportEmailData;

    @BeforeEach
    void setUp() {
        paymentReportEmailData = new PaymentReportEmailData("to", "subject", "link",
                "filename", true);
    }

    @Test
    void getTo() {
        assertThat(paymentReportEmailData.getTo(), is("to"));
    }

    @Test
    void getSubject() {
        assertThat(paymentReportEmailData.getSubject(), is("subject"));
    }

    @Test
    void getFileLink() {
        assertThat(paymentReportEmailData.getFileLink(), is("link"));
    }

    @Test
    void getFileName() {
        assertThat(paymentReportEmailData.getFileName(), is("filename"));
    }

    @Test
    void getHasNoPaymentTransactions() {
        assertThat(paymentReportEmailData.getHasNoPaymentTransactions(), is(true));
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(PaymentReportEmailData.class).suppress(Warning.NONFINAL_FIELDS).usingGetClass()
            .verify();
    }

    @Test
    void builder() {
        assertThat(PaymentReportEmailData.builder(), isA(PaymentReportEmailData.Builder.class));
    }

    @Test
    void build() {
        final PaymentReportEmailData.Builder builder = PaymentReportEmailData.builder();
        final PaymentReportEmailData data =
            builder.withTo("TO").withSubject("SUBJECT").withFileLink("LINK").withFileName("FILENAME")
                    .witHasNoPaymentTransactions(true).build();
        final PaymentReportEmailData expected = new PaymentReportEmailData("TO", "SUBJECT", "LINK",
                "FILENAME", true);

        assertThat(data, is(expected));
    }

    @Test
    void setTo() {
        paymentReportEmailData.setTo("TO");
        assertThat(paymentReportEmailData.getTo(), is("TO"));
    }

    @Test
    void setSubject() {
        paymentReportEmailData.setSubject("SUBJECT");
        assertThat(paymentReportEmailData.getSubject(), is("SUBJECT"));
    }

    @Test
    void setFileLink() {
        paymentReportEmailData.setFileLink("LINK");
        assertThat(paymentReportEmailData.getFileLink(), is("LINK"));
    }

    @Test
    void setFileName() {
        paymentReportEmailData.setFileName("FILENAME");
        assertThat(paymentReportEmailData.getFileName(), is("FILENAME"));
    }

    @Test
    void setHasNoPaymentTransactions() {
        paymentReportEmailData.setHasNoPaymentTransactions(true);
        assertThat(paymentReportEmailData.getHasNoPaymentTransactions(), is(true));
    }
}
