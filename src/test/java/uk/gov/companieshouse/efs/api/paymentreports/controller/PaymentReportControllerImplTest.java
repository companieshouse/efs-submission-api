package uk.gov.companieshouse.efs.api.paymentreports.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.efs.api.paymentreports.service.PaymentReportService;

@ExtendWith(MockitoExtension.class)
class PaymentReportControllerImplTest {
    private PaymentReportController testController;

    @Mock
    private PaymentReportService paymentReportService;

    @BeforeEach
    void setUp() {
        testController = new PaymentReportControllerImpl(paymentReportService);
    }

    @Test
    void sendFinancePaymentReports() {
        final ResponseEntity<Void> response = testController.sendFinancePaymentReports();

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

    }

    @Test
    void sendFinancePaymentReportsWhenExceptionThrown() throws IOException {
        doThrow(new IOException("expected failure")).when(paymentReportService).sendFinancePaymentReports();

        final ResponseEntity<Void> response = testController.sendFinancePaymentReports();

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

    }

    @Test
    void sendScotlandPaymentReport() {
        final ResponseEntity<Void> response = testController.sendScotlandPaymentReport();

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

    }

    @Test
    void sendScotlandPaymentReportWhenExceptionThrown() throws IOException {
        doThrow(new IOException("expected failure")).when(paymentReportService).sendScotlandPaymentReport();

        final ResponseEntity<Void> response = testController.sendScotlandPaymentReport();

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

    }

}