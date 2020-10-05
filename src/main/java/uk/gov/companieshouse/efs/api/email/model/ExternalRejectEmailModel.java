package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class ExternalRejectEmailModel {
    private final Submission submission;
    private final List<String> rejectReasons;

    public ExternalRejectEmailModel(Submission submission, List<String> rejectReasons) {
        this.submission = submission;
        this.rejectReasons = Collections.unmodifiableList(rejectReasons);
    }

    public Submission getSubmission() {
        return submission;
    }

    public List<String> getRejectReasons() {
        return rejectReasons;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExternalRejectEmailModel that = (ExternalRejectEmailModel) o;
        return Objects.equals(getSubmission(), that.getSubmission()) && Objects
            .equals(getRejectReasons(), that.getRejectReasons());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmission(), getRejectReasons());
    }
}
