package uk.gov.companieshouse.efs.api.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Component
@PropertySource("classpath:external.rejected.mail.properties")
@ConfigurationProperties(prefix = "external.rejected.mail")
public class ExternalRejectedEmailConfig  extends EmailConfig {

}
