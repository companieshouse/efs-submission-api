package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class InternalAvFailedEmailModel {
    private final Submission submission;
    private final List<String> infectedFiles;

    public InternalAvFailedEmailModel(Submission submission, List<String> infectedFiles) {
        this.submission = submission;
        this.infectedFiles = Collections.unmodifiableList(infectedFiles);
    }

    public Submission getSubmission() {
        return submission;
    }

    public List<String> getInfectedFiles() {
        return infectedFiles;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InternalAvFailedEmailModel that = (InternalAvFailedEmailModel) o;
        return Objects.equals(getSubmission(), that.getSubmission()) && Objects
            .equals(getInfectedFiles(), that.getInfectedFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmission(), getInfectedFiles());
    }
}
