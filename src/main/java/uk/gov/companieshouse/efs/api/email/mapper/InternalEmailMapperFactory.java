package uk.gov.companieshouse.efs.api.email.mapper;

import org.springframework.stereotype.Component;

@Component
public class InternalEmailMapperFactory {
    private final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper;
    private final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper;
    private final InternalAvFailedEmailMapper internalAvFailedMapper;
    private final InternalFailedConversionEmailMapper internalFailedConversionMapper;
    private final InternalSubmissionEmailMapper internalSubmissionMapper;
    private final PaymentReportEmailMapper paymentReportMapper;

    public InternalEmailMapperFactory(final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper,
        final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper,
        final InternalAvFailedEmailMapper internalAvFailedMapper,
        final InternalFailedConversionEmailMapper internalFailedConversionMapper,
        final InternalSubmissionEmailMapper internalSubmissionMapper,
        final PaymentReportEmailMapper paymentReportMapper) {
        this.delayedSubmissionBusinessMapper = delayedSubmissionBusinessMapper;
        this.delayedSubmissionSupportMapper = delayedSubmissionSupportMapper;
        this.internalAvFailedMapper = internalAvFailedMapper;
        this.internalFailedConversionMapper = internalFailedConversionMapper;
        this.internalSubmissionMapper = internalSubmissionMapper;
        this.paymentReportMapper = paymentReportMapper;
    }

    public DelayedSubmissionBusinessEmailMapper getDelayedSubmissionBusinessMapper() {
        return delayedSubmissionBusinessMapper;
    }

    public DelayedSubmissionSupportEmailMapper getDelayedSubmissionSupportMapper() {
        return delayedSubmissionSupportMapper;
    }

    public InternalAvFailedEmailMapper getInternalAvFailedMapper() {
        return internalAvFailedMapper;
    }

    public InternalFailedConversionEmailMapper getInternalFailedConversionMapper() {
        return internalFailedConversionMapper;
    }

    public InternalSubmissionEmailMapper getInternalSubmissionMapper() {
        return internalSubmissionMapper;
    }

    public PaymentReportEmailMapper getPaymentReportMapper() {
        return paymentReportMapper;
    }
}
