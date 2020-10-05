package uk.gov.companieshouse.efs.api.email.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class PaymentReportEmailModelTest {
    PaymentReportEmailModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new PaymentReportEmailModel("link", "filename", true);
    }

    @Test
    void getFileLink() {
        assertThat(testModel.getFileLink(), is("link"));
    }

    @Test
    void setFileLink() {
        testModel.setFileLink("LINK");
        assertThat(testModel.getFileLink(), is("LINK"));
    }

    @Test
    void getFileName() {
        assertThat(testModel.getFileName(), is("filename"));
    }

    @Test
    void setFileName() {
        testModel.setFileName("FILENAME");
        assertThat(testModel.getFileName(), is("FILENAME"));
    }

    @Test
    void getHasNoPaymentTransactions() {
        assertThat(testModel.getHasNoPaymentTransactions(), is(true));
    }

    @Test
    void setHasNoPaymentTransactions() {
        testModel.setHasNoPaymentTransactions(true);
        assertThat(testModel.getHasNoPaymentTransactions(), is(true));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(PaymentReportEmailModel.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
}