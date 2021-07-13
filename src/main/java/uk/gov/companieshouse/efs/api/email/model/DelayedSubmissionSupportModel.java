package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DelayedSubmissionSupportModel {
    private String submissionId;
    private String confirmationReference;
    private String submittedAt;
    private String customerEmail;
    private String companyNumber;

    public DelayedSubmissionSupportModel(String submissionId, String confirmationReference,
        String submittedAt, final String customerEmail, final String companyNumber) {
        this.submissionId = submissionId;
        this.confirmationReference = confirmationReference;
        this.submittedAt = submittedAt;
        this.customerEmail = customerEmail;
        this.companyNumber = companyNumber;
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

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(final String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
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
        return Objects.equals(getSubmissionId(), that.getSubmissionId()) && Objects.equals(
            getConfirmationReference(), that.getConfirmationReference()) && Objects.equals(
            getSubmittedAt(), that.getSubmittedAt()) && Objects.equals(getCustomerEmail(),
            that.getCustomerEmail()) && Objects.equals(getCompanyNumber(), that.getCompanyNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getConfirmationReference(), getSubmittedAt(),
            getCustomerEmail(), getCompanyNumber());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
