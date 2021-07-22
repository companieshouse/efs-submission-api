package uk.gov.companieshouse.efs.api.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;

/**
 * Instances of this class are responsible for generating a timestamp for to represent the current date and time.
 * 
 * NOTE: For generating UTC timestamps, pass the UTC system clock to the constructor:
 * 
 *      new CurrentTimeStampGenerator(Clock.systemUTC())
 */
@Component
public class CurrentTimestampGenerator implements TimestampGenerator<LocalDateTime> {
    private final Clock clock;

    public CurrentTimestampGenerator(final Clock clock) {
        this.clock = clock;
    }

    @Override
    public LocalDateTime generateTimestamp() {
        return LocalDateTime.now(clock);
    }

}
