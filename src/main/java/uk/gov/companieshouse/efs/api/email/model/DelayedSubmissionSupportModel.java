package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;

public class DelayedSubmissionSupportModel {
    private String submissionId;
    private String confirmationReference;
    private String submittedAt;

    public DelayedSubmissionSupportModel(String submissionId, String confirmationReference, String submittedAt) {
        this.submissionId = submissionId;
        this.confirmationReference = confirmationReference;
        this.submittedAt = submittedAt;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public void setConfirmationReference(String confirmationReference) {
        this.confirmationReference = confirmationReference;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelayedSubmissionSupportModel that = (DelayedSubmissionSupportModel) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId()) && Objects
            .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getSubmittedAt(), that.getSubmittedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getConfirmationReference(), getSubmittedAt());
    }
}
