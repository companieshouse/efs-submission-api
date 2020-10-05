package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.Logger;

class LoggingConfigTest {

    private LoggingConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new LoggingConfig();
    }

    @Test
    void structuredLoggerBean() {
        assertThat(testConfig.logger(), isA(Logger.class));
    }
    
}