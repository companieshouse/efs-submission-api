package uk.gov.companieshouse.efs.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CurrentTimestampGeneratorTest {

    private CurrentTimestampGenerator timestampGenerator;

    @BeforeEach
    void setUp() {
        this.timestampGenerator = new CurrentTimestampGenerator();
    }

    @Test
    void testTimestampGeneratorReturnsTimestamp() {
        //when
        LocalDateTime actual = this.timestampGenerator.generateTimestamp();

        //then
        assertNotNull(actual);
    }
}
