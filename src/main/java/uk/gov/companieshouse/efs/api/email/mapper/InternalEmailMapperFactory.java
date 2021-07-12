package uk.gov.companieshouse.efs.api.email.mapper;

import org.springframework.stereotype.Component;

@Component
public class InternalEmailMapperFactory {
    private final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper;
    private final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper;
    private final DelayedSH19SameDaySubmissionSupportEmailMapper
        delayedSH19SameDaySubmissionSupportEmailMapper;
    private final DelayedSH19SameDaySubmissionBusinessEmailMapper
        delayedSH19SameDaySubmissionBusinessEmailMapper;
    private final InternalAvFailedEmailMapper internalAvFailedMapper;
    private final InternalFailedConversionEmailMapper internalFailedConversionMapper;
    private final InternalSubmissionEmailMapper internalSubmissionMapper;
    private final PaymentReportEmailMapper paymentReportMapper;

    public InternalEmailMapperFactory(
        final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper,
        final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper,
        final DelayedSH19SameDaySubmissionBusinessEmailMapper delayedSH19SameDaySubmissionBusinessEmailMapper,
        final DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper,
        final InternalAvFailedEmailMapper internalAvFailedMapper,
        final InternalFailedConversionEmailMapper internalFailedConversionMapper,
        final InternalSubmissionEmailMapper internalSubmissionMapper,
        final PaymentReportEmailMapper paymentReportMapper) {
        this.delayedSubmissionBusinessMapper = delayedSubmissionBusinessMapper;
        this.delayedSubmissionSupportMapper = delayedSubmissionSupportMapper;
        this.delayedSH19SameDaySubmissionBusinessEmailMapper =
            delayedSH19SameDaySubmissionBusinessEmailMapper;
        this.delayedSH19SameDaySubmissionSupportEmailMapper =
            delayedSH19SameDaySubmissionSupportEmailMapper;
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

    public DelayedSH19SameDaySubmissionBusinessEmailMapper getDelayedSH19SameDaySubmissionBusinessEmailMapper() {
        return delayedSH19SameDaySubmissionBusinessEmailMapper;
    }

    public DelayedSH19SameDaySubmissionSupportEmailMapper getDelayedSH19SameDaySubmissionSupportEmailMapper() {
        return delayedSH19SameDaySubmissionSupportEmailMapper;
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
