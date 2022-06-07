package uk.gov.companieshouse.efs.api.submissions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Document(collection = "submissions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Submission {

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
    @Field("payment_sessions")
    private SessionListApi paymentSessions;
    @Field("form")
    private FormDetails formDetails;
    @Field("chips_reject_reasons")
    private List<RejectReason> chipsRejectReasons;
    @Field("confirm_authorised")
    private Boolean confirmAuthorised;
    @Field("fee_on_submission")
    private String feeOnSubmission;

    private Submission() {
        // no direct instantiation
    }

    public String getId() {
        return id;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public Company getCompany() {
        return company;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public SessionListApi getPaymentSessions() {
        return paymentSessions;
    }

    public FormDetails getFormDetails() {
        return formDetails;
    }

    public List<RejectReason> getChipsRejectReasons() {
        return chipsRejectReasons;
    }

    public Boolean getConfirmAuthorised() {
        return confirmAuthorised;
    }

    public String getFeeOnSubmission() {
        return feeOnSubmission;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Submission copy) {

        return new Builder().withId(copy.getId())
                .withConfirmationReference(copy.getConfirmationReference())
                .withCreatedAt(copy.getCreatedAt())
                .withSubmittedAt(copy.getSubmittedAt())
                .withLastModifiedAt(copy.getLastModifiedAt())
                .withCompany(copy.getCompany())
                .withPresenter(copy.getPresenter())
                .withStatus(copy.getStatus())
                .withPaymentSessions(copy.getPaymentSessions())
                .withFormDetails(copy.getFormDetails())
                .withChipsRejectReasons(copy.getChipsRejectReasons())
                .withConfirmAuthorised(copy.getConfirmAuthorised())
                .withFeeOnSubmission(copy.getFeeOnSubmission());
    }

    public static class Builder {
        private final List<Consumer<Submission>> buildSteps;

        public Builder() {
            buildSteps = new ArrayList<>();
        }

        public Submission.Builder withId(String id) {
            buildSteps.add(data -> data.id = id);
            return this;
        }

        public Submission.Builder withConfirmationReference(String confirmationReference) {
            buildSteps.add(data -> data.confirmationReference = confirmationReference);
            return this;
        }

        public Submission.Builder withCreatedAt(LocalDateTime createdAt) {
            buildSteps.add(data -> data.createdAt = createdAt);
            return this;
        }

        public Submission.Builder withSubmittedAt(LocalDateTime submittedAt) {
            buildSteps.add(data -> data.submittedAt = submittedAt);
            return this;
        }

        public Submission.Builder withLastModifiedAt(LocalDateTime lastModifiedAt) {
            buildSteps.add(data -> data.lastModifiedAt = lastModifiedAt);
            return this;
        }

        public Submission.Builder withCompany(Company company) {
            buildSteps.add(data -> data.company = company);
            return this;
        }

        public Submission.Builder withPresenter(Presenter presenter) {
            buildSteps.add(data -> data.presenter = presenter);
            return this;
        }

        public Submission.Builder withStatus(SubmissionStatus status) {
            buildSteps.add(data -> data.status = status);
            return this;
        }

        public Submission.Builder withPaymentSessions(SessionListApi paymentSessions) {
            buildSteps.add(data -> data.paymentSessions = paymentSessions);
            return this;
        }

        public Submission.Builder withFormDetails(FormDetails formDetails) {
            buildSteps.add(data -> data.formDetails = formDetails);
            return this;
        }

        public Submission.Builder withChipsRejectReasons(List<RejectReason> chipsRejectReasons) {
            buildSteps.add(data -> data.chipsRejectReasons = chipsRejectReasons);
            return this;
        }

        public Submission.Builder withConfirmAuthorised(Boolean confirmAuthorised) {
            buildSteps.add(data -> data.confirmAuthorised = confirmAuthorised);
            return this;
        }

        public Submission.Builder withFeeOnSubmission(String feeOnSubmission) {
            buildSteps.add(data -> data.feeOnSubmission = feeOnSubmission);
            return this;
        }

        public Submission build() {
            final Submission data = new Submission();

            buildSteps.forEach(step -> step.accept(data));

            return data;
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
            && getStatus() == that.getStatus() && Objects.equals(getPaymentSessions(), that.getPaymentSessions())
            && Objects.equals(getFormDetails(), that.getFormDetails()) && Objects
            .equals(getChipsRejectReasons(), that.getChipsRejectReasons()) && Objects
            .equals(getConfirmAuthorised(), that.getConfirmAuthorised()) && Objects
            .equals(getFeeOnSubmission(), that.getFeeOnSubmission());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConfirmationReference(), getCreatedAt(), getSubmittedAt(), getLastModifiedAt(),
            getCompany(), getPresenter(), getStatus(), getPaymentSessions(), getFormDetails(), getChipsRejectReasons(),
            getConfirmAuthorised(), getFeeOnSubmission());
    }
}
