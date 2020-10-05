package uk.gov.companieshouse.efs.api.paymentreports.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("efs-submission-api/payment-reports")
public interface PaymentReportController {

    @PostMapping("/finance")
    ResponseEntity<Void> sendFinancePaymentReports();

    @PostMapping("/scotland")
    ResponseEntity<Void> sendScotlandPaymentReport();

}