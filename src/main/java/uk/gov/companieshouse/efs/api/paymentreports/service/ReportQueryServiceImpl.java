package uk.gov.companieshouse.efs.api.paymentreports.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.paymentreports.mapper.PaymentReportMapper;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@Service
public class ReportQueryServiceImpl {
    private final SubmissionRepository repository;
    private final PaymentReportMapper paymentReportMapper;

    @Autowired
    public ReportQueryServiceImpl(final SubmissionRepository repository,
        final PaymentReportMapper paymentReportMapper) {
        this.repository = repository;
        this.paymentReportMapper = paymentReportMapper;
    }

    public SubmissionRepository getRepository() {
        return repository;
    }

    public PaymentReportMapper getPaymentReportMapper() {
        return paymentReportMapper;
    }

}
