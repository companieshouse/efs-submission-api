package uk.gov.companieshouse.efs.api.submissions.model;

import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.core.mapping.Field;

public class PaymentSession {
    @Field("session_id")
    @NotEmpty(message = "Session ID must not be empty")
    private String id;
    @Field("session_state")
    @NotEmpty(message = "Session state must not be empty")
    private String state;

    public PaymentSession(final String id, final String state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaymentSession that = (PaymentSession) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getState());
    }
}
