package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the internal AV failed email data for notification purposes.
 * @param to recipient email address
 * @param companyNumber company number
 * @param companyName company name
 * @param confirmationReference confirmation reference
 * @param formType form type
 * @param userEmail user email
 * @param rejectionDate rejection date
 * @param subject email subject
 * @param infectedFiles immutable list of infected files (if no infected files, an empty list)
 */
public record InternalAvFailedEmailData(
    String to,
    String companyNumber,
    String companyName,
    String confirmationReference,
    String formType,
    String userEmail,
    String rejectionDate,
    String subject,
    List<String> infectedFiles
) {
    public InternalAvFailedEmailData {
        infectedFiles = List.copyOf(Objects.requireNonNullElse(infectedFiles, Collections.emptyList()));
    }

    /**
     * Builder for InternalAvFailedEmailData record.
     */
    public static class Builder {
        private String to;
        private String companyNumber;
        private String companyName;
        private String confirmationReference;
        private String formType;
        private String userEmail;
        private String rejectionDate;
        private String subject;
        private List<String> infectedFiles;

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
        public Builder withInfectedFiles(List<String> infectedFiles) {
            this.infectedFiles = infectedFiles;
            return this;
        }
        public InternalAvFailedEmailData build() {
            return new InternalAvFailedEmailData(to, companyNumber, companyName, confirmationReference, formType, userEmail, rejectionDate, subject, infectedFiles);
        }
    }

    /**
     * Returns a new builder for InternalAvFailedEmailData.
     */
    public static Builder builder() {
        return new Builder();
    }
}
