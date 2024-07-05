package uk.gov.companieshouse.efs.api.submissions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;

@JsonInclude(Include.NON_NULL)
public final class PresenterApi {
    @JsonProperty("email")
    private @NotEmpty(
            message = "Presenter email must not be empty"
    ) String email;

    public PresenterApi() {
    }

    public PresenterApi(String email) {
        this.email = email;
    }

    public PresenterApi(uk.gov.companieshouse.efs.api.submissions.model.PresenterApi other) {
        this(other.getEmail());
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            uk.gov.companieshouse.efs.api.submissions.model.PresenterApi that = (uk.gov.companieshouse.efs.api.submissions.model.PresenterApi)o;
            return Objects.equals(this.getEmail(), that.getEmail());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getEmail()});
    }
}