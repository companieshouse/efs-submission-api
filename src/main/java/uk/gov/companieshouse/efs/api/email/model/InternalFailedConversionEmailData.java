package uk.gov.companieshouse.efs.api.email.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

    public InternalFailedConversionEmailData() {
        // no direct instantiation
    }

    public String getTo() {
        return to;
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

    public String getUserEmail() {
        return userEmail;
    }

    public String getRejectionDate() {
        return rejectionDate;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getFailedToConvert() {
        return failedToConvert;
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
        private final List<Consumer<InternalFailedConversionEmailData>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public InternalFailedConversionEmailData.Builder withTo(String to) {
            buildSteps.add(data -> data.to = to);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withCompanyNumber(String companyNumber) {
            buildSteps.add(data -> data.companyNumber = companyNumber);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withCompanyName(String companyName) {
            buildSteps.add(data -> data.companyName = companyName);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withConfirmationReference(String confirmationReference) {
            buildSteps.add(data -> data.confirmationReference = confirmationReference);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withFormType(String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withUserEmail(String userEmail) {
            buildSteps.add(data -> data.userEmail = userEmail);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withRejectionDate(String rejectionDate) {
            buildSteps.add(data -> data.rejectionDate = rejectionDate);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withSubject(String subject) {
            buildSteps.add(data -> data.subject = subject);
            return this;
        }

        public InternalFailedConversionEmailData.Builder withFailedToConvert(List<String> failedToConvert) {
            buildSteps.add(data -> data.failedToConvert = failedToConvert);
            return this;
        }

        public InternalFailedConversionEmailData build() {
            final InternalFailedConversionEmailData data = new InternalFailedConversionEmailData();

            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
