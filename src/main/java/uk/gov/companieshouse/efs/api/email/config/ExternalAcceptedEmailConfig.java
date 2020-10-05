package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:external.accepted.mail.properties")
@ConfigurationProperties(prefix = "external.accepted.mail")
public class ExternalAcceptedEmailConfig extends EmailConfig {

}
