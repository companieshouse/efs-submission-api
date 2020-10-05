package uk.gov.companieshouse.efs.api.email.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

public class InternalSubmissionEmailData {

    private String to;
    private String subject;
    @JsonProperty("confirmation_reference")
    private String confirmationReference;
    @JsonProperty("presenter")
    private Presenter presenter;
    @JsonProperty("company")
    private Company company;
    @JsonProperty("form_type")
    private String formType;
    @JsonProperty("email_file_details_list")
    private List<EmailFileDetails> emailFileDetailsList;

    public InternalSubmissionEmailData(String to, String subject, String confirmationReference,
            Presenter presenter, Company company, String formType, List<EmailFileDetails> emailFileDetailsList) {
        this.to = to;
        this.subject = subject;
        this.confirmationReference = confirmationReference;
        this.presenter = presenter;
        this.company = company;
        this.formType = formType;
        this.emailFileDetailsList = emailFileDetailsList;
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

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public void setConfirmationReference(String confirmationReference) {
        this.confirmationReference = confirmationReference;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public List<EmailFileDetails> getEmailFileDetailsList() {
        return emailFileDetailsList;
    }

    public void setEmailFileDetailsList(List<EmailFileDetails> emailFileDetailsList) {
        this.emailFileDetailsList = emailFileDetailsList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, confirmationReference, emailFileDetailsList, formType, presenter, subject, to);
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
        InternalSubmissionEmailData other = (InternalSubmissionEmailData) obj;
        return Objects.equals(company, other.company)
                && Objects.equals(confirmationReference, other.confirmationReference)
                && Objects.equals(emailFileDetailsList, other.emailFileDetailsList)
                && Objects.equals(formType, other.formType) && Objects.equals(presenter, other.presenter)
                && Objects.equals(to, other.to);
    }

    public static InternalSubmissionEmailData.Builder builder() {
        return new InternalSubmissionEmailData.Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private String confirmationReference;
        private Presenter presenter;
        private Company company;
        private String formType;
        private List<EmailFileDetails> emailFileDetailsList;

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withConfirmationReference(String confirmationReference) {
            this.confirmationReference = confirmationReference;
            return this;
        }

        public Builder withPresenter(Presenter presenter) {
            this.presenter = presenter;
            return this;
        }

        public Builder withCompany(Company company) {
            this.company = company;
            return this;
        }

        public Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public Builder withEmailFileDetailsList(
                List<EmailFileDetails> emailFileDetailsList) {
            this.emailFileDetailsList = emailFileDetailsList;
            return this;
        }

        public InternalSubmissionEmailData build() {
            return new InternalSubmissionEmailData(to, subject, confirmationReference, presenter, company, formType,
                    emailFileDetailsList);
        }
    }
}

