package uk.gov.companieshouse.efs.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.efs.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
@PropertySource("classpath:logger.properties")
public class LoggingConfig {

    @Value("${logger.namespace}")
    private String loggerNamespace;

    public LoggingConfig() {
        // blank no-arg constructor
    }

    /**
     * Creates a logger with specified namespace.
     *
     * @return logger
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }

    /**
     * Creates a loggingInterceptor with specified logger.
     *
     * @param logger sets a structured logging logger
     * @return new {@code LoggingInterceptor}
     */
    @Bean("loggingInterceptorBean")
    public LoggingInterceptor loggingInterceptor(Logger logger) {
        return new LoggingInterceptor(logger);
    }
}
