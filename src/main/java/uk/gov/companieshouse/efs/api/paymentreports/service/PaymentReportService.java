package uk.gov.companieshouse.efs.api.paymentreports.service;

import java.io.IOException;
import java.util.List;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;

public interface PaymentReportService {

    void sendScotlandPaymentReport() throws IOException;

    void sendFinancePaymentReports() throws IOException;

    String generateCsvFileContent(List<PaymentTransaction> paymentTransactions) throws IOException;
}
