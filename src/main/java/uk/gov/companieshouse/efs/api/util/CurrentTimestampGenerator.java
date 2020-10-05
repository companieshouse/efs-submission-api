package uk.gov.companieshouse.efs.api.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * Instances of this class are responsible for generating a timestamp for to represent the current date and time.
 */
@Component
public class CurrentTimestampGenerator implements TimestampGenerator<LocalDateTime> {

    @Override
    public LocalDateTime generateTimestamp() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

}
