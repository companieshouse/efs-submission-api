package uk.gov.companieshouse.efs.api.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
    private LocalDateTime nowUTC;

    @BeforeEach
    void setUp() {
        logger =  LoggerFactory.getLogger(CurrentTimestampGeneratorTest.class.getSimpleName());

        nowUTC = LocalDateTime.now(Clock.systemUTC());
        
        // set fixed clock that always returns nowUTC
        clock = Clock.fixed(nowUTC.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        this.timestampGenerator = new CurrentTimestampGenerator(clock);
    }

    @Test
    void generateTimestampReturnsUTC() {
        //when
        LocalDateTime actual = this.timestampGenerator.generateTimestamp();

        //then
        assertThat(actual, is(nowUTC));
    }
}
