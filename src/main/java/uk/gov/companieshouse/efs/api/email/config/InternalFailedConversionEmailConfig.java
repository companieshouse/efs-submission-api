package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:internal.failedconversion.mail.properties")
@ConfigurationProperties(prefix = "internal.failedconversion.mail")
public class InternalFailedConversionEmailConfig  extends EmailConfig {

}
