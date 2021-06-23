package uk.gov.companieshouse.efs.api.email.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@JsonDeserialize(builder = ExternalRejectEmailData.Builder.class)
public class ExternalRejectEmailData {
    private String to;
    private String subject;
    private String companyNumber;
    private String companyName;
    private String confirmationReference;
    private String formType;
    private String rejectionDate;
    private List<String> rejectReasons;

    private ExternalRejectEmailData() {
        // no direct instantiation
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public String getFormType() {
        return formType;
    }

    public String getRejectionDate() {
        return rejectionDate;
    }

    public List<String> getRejectReasons() {
        return Optional.ofNullable(rejectReasons).map(ArrayList::new).orElse(null);
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
                   .equals(getRejectReasons(), that.getRejectReasons());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTo(), getSubject(), getCompanyNumber(), getCompanyName(),
            getConfirmationReference(), getFormType(), getRejectionDate(), getRejectReasons());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Consumer<ExternalRejectEmailData>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public Builder withTo(String to) {
            buildSteps.add(data -> data.to = to);
            return this;
        }

        public Builder withSubject(String subject) {
            buildSteps.add(data -> data.subject = subject);
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            buildSteps.add(data -> data.companyNumber = companyNumber);
            return this;
        }

        public Builder withCompanyName(String companyName) {
            buildSteps.add(data -> data.companyName = companyName);
            return this;
        }

        public Builder withConfirmationReference(String confirmationReference) {
            buildSteps.add(data -> data.confirmationReference = confirmationReference);
            return this;
        }

        public Builder withFormType(String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }

        public Builder withRejectionDate(String rejectionDate) {
            buildSteps.add(data -> data.rejectionDate = rejectionDate);
            return this;
        }

        public Builder withRejectReasons(List<String> rejectReasons) {
            buildSteps.add(data -> data.rejectReasons = new ArrayList<>(rejectReasons));
            return this;
        }

        public ExternalRejectEmailData build() {
            final ExternalRejectEmailData data = new ExternalRejectEmailData();
            
            buildSteps.forEach(step -> step.accept(data));
            
            return data;
        }

    }
}


