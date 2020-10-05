package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;

public class ExternalAcceptEmailData {

    private String to;
    private String subject;
    private String companyNumber;
    private String companyName;
    private String confirmationReference;
    private String formType;
    private String submittedDate;

    public ExternalAcceptEmailData(String to, String subject, String companyNumber, String companyName, String confirmationReference, String formType, String submittedDate) {
        this.to = to;
        this.subject = subject;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.confirmationReference = confirmationReference;
        this.formType = formType;
        this.submittedDate = submittedDate;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExternalAcceptEmailData that = (ExternalAcceptEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
                   .equals(getCompanyName(), that.getCompanyName()) && Objects
                   .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getFormType(), that.getFormType()) && Objects
                   .equals(getSubmittedDate(), that.getSubmittedDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getCompanyNumber(), getCompanyName(),
            getConfirmationReference(), getFormType(), getSubmittedDate());
    }

    public static ExternalAcceptEmailData.Builder builder() {
        return new ExternalAcceptEmailData.Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private String companyNumber;
        private String companyName;
        private String confirmationReference;
        private String formType;
        private String submittedDate;

        public ExternalAcceptEmailData.Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public ExternalAcceptEmailData.Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public ExternalAcceptEmailData.Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public ExternalAcceptEmailData.Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public ExternalAcceptEmailData.Builder withConfirmationReference(String confirmationReference) {
            this.confirmationReference = confirmationReference;
            return this;
        }

        public ExternalAcceptEmailData.Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public ExternalAcceptEmailData.Builder withSubmittedDate(String submittedDate) {
            this.submittedDate = submittedDate;
            return this;
        }

        public ExternalAcceptEmailData build() {
            return new ExternalAcceptEmailData(to, subject, companyNumber, companyName, confirmationReference, formType, submittedDate);
        }

    }

}
