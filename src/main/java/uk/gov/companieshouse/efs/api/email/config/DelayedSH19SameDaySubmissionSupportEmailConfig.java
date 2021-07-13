package uk.gov.companieshouse.efs.api.email.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:delayed.sh19.sameday.submission.support.mail.properties")
@ConfigurationProperties(prefix = "delayed.sh19.sameday.submission.support.mail")
public class DelayedSH19SameDaySubmissionSupportEmailConfig extends EmailConfig {

    private String supportEmailAddress;

    public String getSupportEmailAddress() {
        return supportEmailAddress;
    }

    public void setSupportEmailAddress(String supportEmailAddress) {
        this.supportEmailAddress = supportEmailAddress;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DelayedSH19SameDaySubmissionSupportEmailConfig
            that = (DelayedSH19SameDaySubmissionSupportEmailConfig) o;
        return Objects.equals(getSupportEmailAddress(), that.getSupportEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSupportEmailAddress());
    }
}
