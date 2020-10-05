package uk.gov.companieshouse.efs.api.email.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

public class ExternalConfirmationEmailData {

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

    public ExternalConfirmationEmailData(String to, String subject, String confirmationReference,
                                         Presenter presenter, Company company, String formType,
                                         List<EmailFileDetails> emailFileDetailsList) {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExternalConfirmationEmailData that = (ExternalConfirmationEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getPresenter(), that.getPresenter()) && Objects
                   .equals(getCompany(), that.getCompany()) && Objects
                   .equals(getFormType(), that.getFormType()) && Objects
                   .equals(getEmailFileDetailsList(), that.getEmailFileDetailsList());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getTo(), getSubject(), getConfirmationReference(), getPresenter(), getCompany(),
                getFormType(), getEmailFileDetailsList());
    }

    public static ExternalConfirmationEmailData.Builder builder() {
        return new ExternalConfirmationEmailData.Builder();
    }

    public static class Builder {

        private String to;
        private String subject;
        private String confirmationReference;
        private Presenter presenter;
        private Company company;
        private String formType;
        private List<EmailFileDetails> emailFileDetailsList;

        public ExternalConfirmationEmailData.Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withConfirmationReference(String confirmationReference) {
            this.confirmationReference = confirmationReference;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withPresenter(Presenter presenter) {
            this.presenter = presenter;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withCompany(Company company) {
            this.company = company;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public ExternalConfirmationEmailData.Builder withEmailFileDetailsList(List<EmailFileDetails> emailFileDetailsList) {
            this.emailFileDetailsList = emailFileDetailsList;
            return this;
        }

        public ExternalConfirmationEmailData build() {
            return new ExternalConfirmationEmailData(to, subject, confirmationReference, presenter, company, formType,
                    emailFileDetailsList);
        }

    }

}
