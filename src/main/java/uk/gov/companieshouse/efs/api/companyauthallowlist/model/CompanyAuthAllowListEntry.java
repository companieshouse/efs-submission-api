package uk.gov.companieshouse.efs.api.companyauthallowlist.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Builder class for the {@code CompanyAuthAllowListEntry}.
 */
@Document(collection = "company_auth_allow_list")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyAuthAllowListEntry {

    private CompanyAuthAllowListEntry() {
        // required by Spring Data
    }

    @JsonProperty("emailAddress")
    private String emailAddress;

    /**
     * Constructor which sets the submission form data.
     * @param emailAddress the email address
     */
    public CompanyAuthAllowListEntry(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setEmailAddress(final String newEmailAddress) {
        this.emailAddress = newEmailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompanyAuthAllowListEntry that = (CompanyAuthAllowListEntry) o;
        return Objects.equals(getEmailAddress(), that.getEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmailAddress());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("emailAddress", getEmailAddress())
                .toString();
    }
}
