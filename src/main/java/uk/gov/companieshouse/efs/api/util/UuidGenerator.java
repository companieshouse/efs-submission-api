package uk.gov.companieshouse.efs.api.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidGenerator implements IdentifierGeneratable {

    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
