package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class ExternalNotificationEmailModel {

    private final Submission submission;

    public ExternalNotificationEmailModel(Submission submission) {
        this.submission = submission;
    }

    public Submission getSubmission() {
        return submission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(submission);
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
        ExternalNotificationEmailModel other = (ExternalNotificationEmailModel) obj;
        return Objects.equals(submission, other.submission);
    }

}
