package uk.gov.companieshouse.efs.api.submissions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;

@Document(collection = "submissions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Submission {

    private static final int REDACT_LEN = 6;
    private static final char REDACT_CHAR = '.';
    private static final String REDACT_MASK = StringUtils.repeat(REDACT_CHAR, REDACT_LEN);

    @Id
    private String id;
    @Field("confirmation_reference")
    private String confirmationReference;
    @Field("created_at")
    private LocalDateTime createdAt;
    @Field("submitted_at")
    private LocalDateTime submittedAt;
    @Field("last_modified_at")
    private LocalDateTime lastModifiedAt;
    private Company company;
    private Presenter presenter;
    private SubmissionStatus status;
    @Field("payment_reference")
    private String paymentReference;
    @Field("form")
    private FormDetails formDetails;
    @Field("chips_reject_reasons")
    private List<RejectReason> chipsRejectReasons;
    @Field("confirm_authorised")
    private Boolean confirmAuthorised;
    @Field("fee_on_submission")
    private String feeOnSubmission;

    public Submission(String id, String confirmationReference, LocalDateTime createdAt,
                      LocalDateTime submittedAt, LocalDateTime lastModifiedAt, Company company,
                      Presenter presenter, SubmissionStatus status, String paymentReference,
                      FormDetails formDetails, List<RejectReason> chipsRejectReasons,
                      Boolean confirmAuthorised, String feeOnSubmission) {
        this.id = id;
        this.confirmationReference = confirmationReference;
        this.createdAt = createdAt;
        this.submittedAt = submittedAt;
        this.lastModifiedAt = lastModifiedAt;
        this.company = company;
        this.presenter = presenter;
        this.status = status;
        this.paymentReference = paymentReference;
        this.formDetails = formDetails;
        this.chipsRejectReasons = chipsRejectReasons;
        this.confirmAuthorised = confirmAuthorised;
        this.feeOnSubmission = feeOnSubmission;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public void setConfirmationReference(final String confirmationReference) {
        this.confirmationReference = confirmationReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public FormDetails getFormDetails() {
        return formDetails;
    }

    public void setFormDetails(FormDetails formDetails) {
        this.formDetails = formDetails;
    }

    public List<RejectReason> getChipsRejectReasons() {
        return chipsRejectReasons;
    }

    public void setChipsRejectReasons(List<RejectReason> chipsRejectReasons) {
        this.chipsRejectReasons = chipsRejectReasons;
    }

    public Boolean getConfirmAuthorised() {
        return confirmAuthorised;
    }

    public void setConfirmAuthorised(final Boolean confirmAuthorised) {
        this.confirmAuthorised = confirmAuthorised;
    }

    public String getFeeOnSubmission() {
        return feeOnSubmission;
    }

    public void setFeeOnSubmission(final String feeOnSubmission) {
        this.feeOnSubmission = feeOnSubmission;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String confirmationReference;
        private LocalDateTime createdAt;
        private LocalDateTime submittedAt;
        private LocalDateTime lastModifiedAt;
        private Company company;
        private Presenter presenter;
        private SubmissionStatus status;
        private String paymentReference;
        private FormDetails formDetails;
        private List<RejectReason> chipsRejectReasons;
        private Boolean confirmAuthorised;
        private String feeOnSubmission;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withConfirmationReference(String confirmationReference) {
            this.confirmationReference = confirmationReference;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public Builder withLastModifiedAt(LocalDateTime lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
            return this;
        }

        public Builder withCompany(Company company) {
            this.company = company;
            return this;
        }

        public Builder withPresenter(Presenter presenter) {
            this.presenter = presenter;
            return this;
        }

        public Builder withStatus(SubmissionStatus status) {
            this.status = status;
            return this;
        }

        public Builder withPaymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
            return this;
        }

        public Builder withFormDetails(FormDetails formDetails) {
            this.formDetails = formDetails;
            return this;
        }

        public Builder withChipsRejectReasons(List<RejectReason> chipsRejectReasons) {
            this.chipsRejectReasons = chipsRejectReasons;
            return this;
        }

        public Builder withConfirmAuthorised(Boolean confirmAuthorised) {
            this.confirmAuthorised = confirmAuthorised;
            return this;
        }

        public Builder withFeeOnSubmission(String feeOnSubmission) {
            this.feeOnSubmission = feeOnSubmission;
            return this;
        }

        public Submission build() {
            return new Submission(id, confirmationReference, createdAt, submittedAt, lastModifiedAt,
                company, presenter, status, paymentReference, formDetails, chipsRejectReasons,
                confirmAuthorised, feeOnSubmission);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Submission that = (Submission) o;
        return Objects.equals(getId(), that.getId()) && Objects
            .equals(getConfirmationReference(), that.getConfirmationReference()) && Objects
            .equals(getCreatedAt(), that.getCreatedAt()) && Objects.equals(getSubmittedAt(), that.getSubmittedAt())
            && Objects.equals(getLastModifiedAt(), that.getLastModifiedAt()) && Objects
            .equals(getCompany(), that.getCompany()) && Objects.equals(getPresenter(), that.getPresenter())
            && getStatus() == that.getStatus() && Objects.equals(getPaymentReference(), that.getPaymentReference())
            && Objects.equals(getFormDetails(), that.getFormDetails()) && Objects
            .equals(getChipsRejectReasons(), that.getChipsRejectReasons()) && Objects
            .equals(getConfirmAuthorised(), that.getConfirmAuthorised()) && Objects
            .equals(getFeeOnSubmission(), that.getFeeOnSubmission());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConfirmationReference(), getCreatedAt(), getSubmittedAt(), getLastModifiedAt(),
            getCompany(), getPresenter(), getStatus(), getPaymentReference(), getFormDetails(), getChipsRejectReasons(),
            getConfirmAuthorised(), getFeeOnSubmission());
    }
}
