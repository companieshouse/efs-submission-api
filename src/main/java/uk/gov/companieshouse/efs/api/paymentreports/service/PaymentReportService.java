package uk.gov.companieshouse.efs.api.paymentreports.service;

import java.io.IOException;

public interface PaymentReportService {

    void sendScotlandPaymentReport() throws IOException;

    void sendFinancePaymentReports() throws IOException;
}
