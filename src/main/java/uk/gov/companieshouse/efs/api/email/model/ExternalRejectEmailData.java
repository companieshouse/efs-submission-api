package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

public class ExternalRejectEmailData {
    private String to;
    private String subject;
    private String companyNumber;
    private String companyName;
    private String confirmationReference;
    private String formType;
    private String rejectionDate;
    private List<String> rejectReasons;
    private boolean isPaidForm;

    public ExternalRejectEmailData(String to, String subject, String companyNumber, String companyName, String confirmationReference, String formType, String rejectionDate, List<String> rejectReasons, boolean isPaidForm) {
        this.to = to;
        this.subject = subject;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.confirmationReference = confirmationReference;
        this.formType = formType;
        this.rejectionDate = rejectionDate;
        this.rejectReasons = rejectReasons;
        this.isPaidForm = isPaidForm;
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

    public String getRejectionDate() {
        return rejectionDate;
    }

    public void setRejectionDate(String rejectionDate) {
        this.rejectionDate = rejectionDate;
    }

    public List<String> getRejectReasons() {
        return rejectReasons;
    }

    public void setRejectReasons(List<String> rejectReasons) {
        this.rejectReasons = rejectReasons;
    }

    public boolean isPaidForm() {
        return isPaidForm;
    }

    public void setPaidForm(boolean paidForm) {
        isPaidForm = paidForm;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExternalRejectEmailData that = (ExternalRejectEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
                   .equals(getCompanyName(), that.getCompanyName()) && Objects
                   .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getFormType(), that.getFormType()) && Objects
                   .equals(getRejectionDate(), that.getRejectionDate()) && Objects
                   .equals(getRejectReasons(), that.getRejectReasons()) && Objects
                   .equals(isPaidForm(), that.isPaidForm());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getCompanyNumber(), getCompanyName(),
            getConfirmationReference(), getFormType(), getRejectionDate(), getRejectReasons(),
                isPaidForm());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private String companyNumber;
        private String companyName;
        private String confirmationReference;
        private String formType;
        private String rejectionDate;
        private List<String> rejectReasons;
        private boolean isPaidForm;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
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

        public Builder withRejectionDate(String rejectionDate) {
            this.rejectionDate = rejectionDate;
            return this;
        }

        public Builder withRejectReasons(List<String> rejectReasons) {
            this.rejectReasons = rejectReasons;
            return this;
        }

        public Builder withIsPaidForm(boolean isPaidForm) {
            this.isPaidForm = isPaidForm;
            return this;
        }

        public ExternalRejectEmailData build() {
            return new ExternalRejectEmailData(to, subject, companyNumber, companyName, confirmationReference, formType, rejectionDate, rejectReasons, isPaidForm);
        }

    }
}


