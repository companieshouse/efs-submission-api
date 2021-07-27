package uk.gov.companieshouse.efs.api.util;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Instances of this class are responsible for generating a timestamp for to represent the current instant (.
 * 
 * NOTE: For generating UTC timestamps, pass the UTC system clock to the constructor:
 * 
 *      new CurrentTimeStampGenerator(Clock.systemUTC())
 */
@Component
public class CurrentTimestampGenerator implements TimestampGenerator<Instant> {
    private final Clock clock;

    public CurrentTimestampGenerator(final Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant generateTimestamp() {
        return Instant.now(clock);
    }

}
