package uk.gov.companieshouse.efs.api.email.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:payment.report.mail.properties")
@ConfigurationProperties(prefix = "payment.report.mail")
public class PaymentReportEmailConfig extends EmailConfig {

    private String financeEmailAddress;
    private String scottishEmailAddress;
    private String specialCapitalEmailAddress;

    public String getFinanceEmailAddress() {
        return financeEmailAddress;
    }

    public void setFinanceEmailAddress(final String financeEmailAddress) {
        this.financeEmailAddress = financeEmailAddress;
    }

    public String getScottishEmailAddress() {
        return scottishEmailAddress;
    }

    public void setScottishEmailAddress(String scottishEmailAddress) {
        this.scottishEmailAddress = scottishEmailAddress;
    }

    public String getSpecialCapitalEmailAddress() {
        return specialCapitalEmailAddress;
    }

    public void setSpecialCapitalEmailAddress(final String specialCapitalEmailAddress) {
        this.specialCapitalEmailAddress = specialCapitalEmailAddress;
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
        final PaymentReportEmailConfig that = (PaymentReportEmailConfig) o;
        return Objects.equals(getFinanceEmailAddress(), that.getFinanceEmailAddress()) &&
               Objects.equals(getScottishEmailAddress(), that.getScottishEmailAddress()) &&
               Objects.equals(getSpecialCapitalEmailAddress(), that.getSpecialCapitalEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
            getFinanceEmailAddress(),
            getScottishEmailAddress(),
            getSpecialCapitalEmailAddress());
    }
}