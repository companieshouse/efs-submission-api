package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Email configuration POJO; not a Spring @Configuration.
 */
@Component
@PropertySource("classpath:internal.submission.mail.properties")
@ConfigurationProperties(prefix = "internal.submission.mail")
public class InternalSubmissionEmailConfig  extends EmailConfig {

}
