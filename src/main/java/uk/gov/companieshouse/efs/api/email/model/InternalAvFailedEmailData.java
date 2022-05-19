package uk.gov.companieshouse.efs.api.email.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class InternalAvFailedEmailData {
    private String to;
    private String companyNumber;
    private String companyName;
    private String confirmationReference;
    private String formType;
    private String userEmail;
    private String rejectionDate;
    private String subject;
    private List<String> infectedFiles;

    public InternalAvFailedEmailData() {
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

    public List<String> getInfectedFiles() {
        return infectedFiles;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InternalAvFailedEmailData that = (InternalAvFailedEmailData) o;
        return Objects.equals(getTo(), that.getTo()) && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
                   .equals(getCompanyName(), that.getCompanyName()) && Objects
                   .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
                   .equals(getFormType(), that.getFormType()) && Objects
                   .equals(getUserEmail(), that.getUserEmail()) && Objects
                   .equals(getRejectionDate(), that.getRejectionDate()) && Objects
                   .equals(getSubject(), that.getSubject()) && Objects
                   .equals(getInfectedFiles(), that.getInfectedFiles());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getTo(), getCompanyNumber(), getCompanyName(), getConfirmationReference(),
                getFormType(), getUserEmail(), getRejectionDate(), getSubject(),
                getInfectedFiles());
    }

    public static class Builder {
        private final List<Consumer<InternalAvFailedEmailData>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public InternalAvFailedEmailData.Builder withTo(String to) {
            buildSteps.add(data -> data.to = to);
            return this;
        }

        public InternalAvFailedEmailData.Builder withCompanyNumber(String companyNumber) {
            buildSteps.add(data -> data.companyNumber = companyNumber);
            return this;
        }

        public InternalAvFailedEmailData.Builder withCompanyName(String companyName) {
            buildSteps.add(data -> data.companyName = companyName);
            return this;
        }

        public InternalAvFailedEmailData.Builder withConfirmationReference(String confirmationReference) {
            buildSteps.add(data -> data.confirmationReference = confirmationReference);
            return this;
        }

        public InternalAvFailedEmailData.Builder withFormType(String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }

        public InternalAvFailedEmailData.Builder withUserEmail(String userEmail) {
            buildSteps.add(data -> data.userEmail = userEmail);
            return this;
        }

        public InternalAvFailedEmailData.Builder withRejectionDate(String rejectionDate) {
            buildSteps.add(data -> data.rejectionDate = rejectionDate);
            return this;
        }

        public InternalAvFailedEmailData.Builder withSubject(String subject) {
            buildSteps.add(data -> data.subject = subject);
            return this;
        }

        public InternalAvFailedEmailData.Builder withInfectedFiles(List<String> infectedFiles) {
            buildSteps.add(data -> data.infectedFiles = infectedFiles);
            return this;
        }

        public InternalAvFailedEmailData build() {
            final InternalAvFailedEmailData data = new InternalAvFailedEmailData();

            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
