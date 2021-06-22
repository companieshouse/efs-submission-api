package uk.gov.companieshouse.efs.api.email.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

@JsonDeserialize(builder = ExternalConfirmationEmailData.Builder.class)
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
    @JsonProperty("top_level_category")
    private CategoryTypeConstants topLevelCategory;
    @JsonProperty("email_file_details_list")
    private List<EmailFileDetails> emailFileDetailsList;
    @JsonProperty("fee_on_submission")
    private String feeOnSubmission;

    private ExternalConfirmationEmailData() {
        // no direct instantiation
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public Company getCompany() {
        return company;
    }

    public String getFormType() {
        return formType;
    }

    public CategoryTypeConstants getTopLevelCategory() {
        return topLevelCategory;
    }

    public List<EmailFileDetails> getEmailFileDetailsList() {
        return Optional.ofNullable(emailFileDetailsList).map(ArrayList::new).orElse(null);
    }

    public String getFeeOnSubmission() {
        return feeOnSubmission;
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
                   .equals(getTopLevelCategory(), that.getTopLevelCategory())  && Objects
                   .equals(getEmailFileDetailsList(), that.getEmailFileDetailsList()) && Objects
                   .equals(getFeeOnSubmission(), that.getFeeOnSubmission());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getTo(), getSubject(), getConfirmationReference(), getPresenter(), getCompany(),
                getFormType(), getTopLevelCategory(), getEmailFileDetailsList(), getFeeOnSubmission());
    }

    public static ExternalConfirmationEmailData.Builder builder() {
        return new ExternalConfirmationEmailData.Builder();
    }

    public static class Builder {

        private final List<Consumer<ExternalConfirmationEmailData>> buildSteps;

        private Builder() {
            this.buildSteps = new ArrayList<>();
        }
        
        public ExternalConfirmationEmailData.Builder withTo(String to) {
            buildSteps.add(data -> data.to = to);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withSubject(String subject) {
            buildSteps.add(data -> data.subject = subject);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withConfirmationReference(String confirmationReference) {
            buildSteps.add(data -> data.confirmationReference = confirmationReference);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withPresenter(Presenter presenter) {
            buildSteps.add(data -> data.presenter = presenter);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withCompany(Company company) {
            buildSteps.add(data -> data.company = company);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withFormType(String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withTopLevelCategory(
            CategoryTypeConstants topLevelCategory) {
            buildSteps.add(data -> data.topLevelCategory = topLevelCategory);
            return this;
        }

        public ExternalConfirmationEmailData.Builder withEmailFileDetailsList(List<EmailFileDetails> emailFileDetailsList) {
            buildSteps.add(
                data -> data.emailFileDetailsList = new ArrayList<>(emailFileDetailsList));
            return this;
        }

        public ExternalConfirmationEmailData.Builder withFeeOnSubmission(String feeOnSubmission) {
            buildSteps.add(data -> data.feeOnSubmission = feeOnSubmission);
            return this;
        }
        
        public ExternalConfirmationEmailData build() {
            ExternalConfirmationEmailData data = new ExternalConfirmationEmailData();
            
            buildSteps.forEach(step -> step.accept(data));
            
            return data;
        }

    }

}
