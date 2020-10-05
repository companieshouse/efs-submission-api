package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:delayed.submission.business.mail.properties")
@ConfigurationProperties(prefix = "delayed.submission.business.mail")
public class DelayedSubmissionBusinessEmailConfig extends EmailConfig {
}
