package uk.gov.companieshouse.efs.api.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class CurrentTimestampGeneratorTest {

    private CurrentTimestampGenerator timestampGenerator;
    
    private Logger logger;
    private Clock clock;
    private Instant nowUTC;

    @BeforeEach
    void setUp() {
        logger =  LoggerFactory.getLogger(CurrentTimestampGeneratorTest.class.getSimpleName());

        nowUTC = Instant.now();
        // set fixed clock that always returns nowUTC
        clock = Clock.fixed(nowUTC, ZoneId.of("UTC"));
        
        this.timestampGenerator = new CurrentTimestampGenerator(clock);
    }

    @Test
    void generateTimestampReturnsUTC() {
        //when
        Instant actual = this.timestampGenerator.generateTimestamp();

        //then
        assertThat(actual, is(nowUTC));
    }
}
