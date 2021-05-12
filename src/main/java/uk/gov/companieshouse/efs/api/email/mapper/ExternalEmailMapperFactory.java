package uk.gov.companieshouse.efs.api.email.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ExternalEmailMapperFactory {
    private final ExternalAcceptEmailMapper acceptEmailMapper;
    private final ExternalNotificationEmailMapper confirmationMapper;
    private final ExternalNotificationEmailMapper paymentFailedMapper;
    private final ExternalRejectEmailMapper rejectMapper;

    public ExternalEmailMapperFactory(final ExternalAcceptEmailMapper acceptEmailMapper,
        @Qualifier("confirmationEmailMapper")
        final ExternalNotificationEmailMapper confirmationMapper,
        @Qualifier("paymentFailedEmailMapper")
        final ExternalNotificationEmailMapper paymentFailedMapper,
        final ExternalRejectEmailMapper rejectMapper) {
        this.acceptEmailMapper = acceptEmailMapper;
        this.confirmationMapper = confirmationMapper;
        this.paymentFailedMapper = paymentFailedMapper;
        this.rejectMapper = rejectMapper;
    }

    public ExternalAcceptEmailMapper getAcceptEmailMapper() {
        return acceptEmailMapper;
    }

    public ExternalNotificationEmailMapper getConfirmationMapper() {
        return confirmationMapper;
    }

    public ExternalNotificationEmailMapper getPaymentFailedMapper() {
        return paymentFailedMapper;
    }

    public ExternalRejectEmailMapper getRejectMapper() {
        return rejectMapper;
    }
}
