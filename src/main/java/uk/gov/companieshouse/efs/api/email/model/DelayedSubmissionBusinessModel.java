package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;

public class DelayedSubmissionBusinessModel {

    private String confirmationReference;
    private String companyNumber;
    private String formType;
    private String email;
    private String submissionDate;

    /**
     * Constructor.
     *
     * @param confirmationReference dependency
     * @param companyNumber         dependency
     * @param formType              dependency
     * @param email                 dependency
     * @param submissionDate        dependency
     */
    public DelayedSubmissionBusinessModel(String confirmationReference, String companyNumber, String formType, String email, String submissionDate) {
        this.confirmationReference = confirmationReference;
        this.companyNumber = companyNumber;
        this.formType = formType;
        this.email = email;
        this.submissionDate = submissionDate;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public void setConfirmationReference(String confirmationReference) {
        this.confirmationReference = confirmationReference;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DelayedSubmissionBusinessModel that = (DelayedSubmissionBusinessModel) o;
        return Objects.equals(getConfirmationReference(), that.getConfirmationReference())
               && Objects.equals(getCompanyNumber(), that.getCompanyNumber())
               && Objects.equals(getFormType(), that.getFormType())
               && Objects.equals(getEmail(), that.getEmail())
               && Objects.equals(getSubmissionDate(), that.getSubmissionDate());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getConfirmationReference(), getCompanyNumber(), getFormType(), getEmail(),
                getSubmissionDate());
    }
}
