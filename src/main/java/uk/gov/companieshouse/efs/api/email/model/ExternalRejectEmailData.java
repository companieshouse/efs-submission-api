package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the data for an external reject email.
 * @param to recipient email address
 * @param subject email subject
 * @param companyNumber company number
 * @param companyName company name
 * @param confirmationReference confirmation reference
 * @param formType form type
 * @param rejectionDate rejection date
 * @param rejectReasons immutable list of reject reasons - if none, an empty list is created
 * @param isPaidForm whether the form is paid
 */
public record ExternalRejectEmailData(
    String to,
    String subject,
    String companyNumber,
    String companyName,
    String confirmationReference,
    String formType,
    String rejectionDate,
    List<String> rejectReasons,
    boolean isPaidForm
) {
    public ExternalRejectEmailData {
        rejectReasons = List.copyOf(Objects.requireNonNullElse(rejectReasons, Collections.emptyList()));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ExternalRejectEmailData record.
     */
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
            return new ExternalRejectEmailData(
                    to,
                    subject,
                    companyNumber,
                    companyName,
                    confirmationReference,
                    formType,
                    rejectionDate,
                    rejectReasons,
                    isPaidForm
            );
        }
    }
}

