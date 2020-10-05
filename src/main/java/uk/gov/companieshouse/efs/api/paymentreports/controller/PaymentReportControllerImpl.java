package uk.gov.companieshouse.efs.api.paymentreports.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.efs.api.paymentreports.service.PaymentReportService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class PaymentReportControllerImpl implements PaymentReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final PaymentReportService paymentReportService;

    @Autowired
    public PaymentReportControllerImpl(final PaymentReportService paymentReportService) {
        this.paymentReportService = paymentReportService;
    }

    @Override
    public ResponseEntity<Void> sendFinancePaymentReports() {

        try {
            paymentReportService.sendFinancePaymentReports();
        } catch (IOException ex) {
            // Logged as error in PaymentReportService.writeCsvFile
            LOGGER.info("Unable to send finance payment reports" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> sendScotlandPaymentReport() {
        try {
            paymentReportService.sendScotlandPaymentReport();
        } catch (IOException ex) {
            // Logged as error in PaymentReportService.writeCsvFile
            LOGGER.info("Unable to send scotland payment report" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}