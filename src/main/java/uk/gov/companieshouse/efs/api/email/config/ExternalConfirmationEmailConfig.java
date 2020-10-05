package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Email configuration POJO; not a Spring @Configuration.
 */
@Component
@PropertySource("classpath:external.confirmation.mail.properties")
@ConfigurationProperties(prefix = "external.confirmation.mail")
public class ExternalConfirmationEmailConfig extends EmailConfig {

}
