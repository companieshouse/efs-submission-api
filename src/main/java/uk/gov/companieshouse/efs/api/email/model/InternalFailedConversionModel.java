package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class InternalFailedConversionModel {
    private Submission submission;
    private List<String> failedToConvert;

    public InternalFailedConversionModel(Submission submission, List<String> failedToConvert) {
        this.submission = submission;
        this.failedToConvert = failedToConvert;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public List<String> getFailedToConvert() {
        return failedToConvert;
    }

    public void setFailedToConvert(List<String> failedToConvert) {
        this.failedToConvert = failedToConvert;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InternalFailedConversionModel that = (InternalFailedConversionModel) o;
        return Objects.equals(getSubmission(), that.getSubmission()) && Objects
            .equals(getFailedToConvert(), that.getFailedToConvert());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmission(), getFailedToConvert());
    }
}
