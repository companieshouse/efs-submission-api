package uk.gov.companieshouse.efs.api.email.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:delayed.sh19.sameday.submission.business.mail.properties")
@ConfigurationProperties(prefix = "delayed.sh19.sameday.submission.business.mail")
public class DelayedSH19SameDaySubmissionBusinessEmailConfig extends EmailConfig {
}
