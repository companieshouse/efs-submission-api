package uk.gov.companieshouse.efs.api.util;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

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
