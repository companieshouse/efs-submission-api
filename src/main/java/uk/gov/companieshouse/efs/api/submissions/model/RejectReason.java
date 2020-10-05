package uk.gov.companieshouse.efs.api.submissions.model;

import java.util.Objects;

public class RejectReason {

    private String reason;

    public RejectReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RejectReason other = (RejectReason) obj;
        return Objects.equals(reason, other.reason);
    }

}
