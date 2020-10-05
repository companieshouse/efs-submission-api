package uk.gov.companieshouse.efs.api.email.mapper;

import org.springframework.stereotype.Component;

@Component
public class ExternalEmailMapperFactory {
    private final ExternalAcceptEmailMapper acceptEmailMapper;
    private final ExternalConfirmationEmailMapper confirmationMapper;
    private final ExternalRejectEmailMapper rejectMapper;

    public ExternalEmailMapperFactory(final ExternalAcceptEmailMapper acceptEmailMapper,
        final ExternalConfirmationEmailMapper confirmationMapper, final ExternalRejectEmailMapper rejectMapper) {
        this.acceptEmailMapper = acceptEmailMapper;
        this.confirmationMapper = confirmationMapper;
        this.rejectMapper = rejectMapper;
    }

    public ExternalAcceptEmailMapper getAcceptEmailMapper() {
        return acceptEmailMapper;
    }

    public ExternalConfirmationEmailMapper getConfirmationMapper() {
        return confirmationMapper;
    }

    public ExternalRejectEmailMapper getRejectMapper() {
        return rejectMapper;
    }
}
