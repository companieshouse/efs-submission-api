package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

public class InternalFailedConversionEmailData {
    private String to;
    private String companyNumber;
    private String companyName;
    private String confirmationReference;
    private String formType;
    private String userEmail;
    private String rejectionDate;
    private String subject;
    private List<String> failedToConvert;

    public InternalFailedConversionEmailData(String to, String companyNumber, String companyName, String confirmationReference, String formType, String userEmail, String rejectionDate, String subject, List<String> failedToConvert) {
        this.to = to;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.confirmationReference = confirmationReference;
        this.formType = formType;
        this.userEmail = userEmail;
        this.rejectionDate = rejectionDate;
        this.subject = subject;
        this.failedToConvert = failedToConvert;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public void setConfirmationReference(String confirmationReference) {
        this.confirmationReference = confirmationReference;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getRejectionDate() {
        return rejectionDate;
    }

    public void setRejectionDate(String rejectionDate) {
        this.rejectionDate = rejectionDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
        final InternalFailedConversionEmailData that = (InternalFailedConversionEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
                   .equals(getCompanyName(), that.getCompanyName()) && Objects
                   .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getFormType(), that.getFormType()) && Objects
                   .equals(getUserEmail(), that.getUserEmail()) && Objects
                   .equals(getRejectionDate(), that.getRejectionDate()) && Objects
                   .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getFailedToConvert(), that.getFailedToConvert());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getTo(), getCompanyNumber(), getCompanyName(), getConfirmationReference(),
                getFormType(), getUserEmail(), getRejectionDate(), getSubject(),
                getFailedToConvert());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String companyNumber;
        private String companyName;
        private String confirmationReference;
        private String formType;
        private String userEmail;
        private String rejectionDate;
        private String subject;
        private List<String> failedToConvert;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withConfirmationReference(String confirmationReference) {
            this.confirmationReference = confirmationReference;
            return this;
        }

        public Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public Builder withUserEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder withRejectionDate(String rejectionDate) {
            this.rejectionDate = rejectionDate;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withFailedToConvert(List<String> failedToConvert) {
            this.failedToConvert = failedToConvert;
            return this;
        }

        public InternalFailedConversionEmailData build() {
            return new InternalFailedConversionEmailData(to, companyNumber, companyName, confirmationReference, formType, userEmail, rejectionDate, subject, failedToConvert);
        }
    }
}
