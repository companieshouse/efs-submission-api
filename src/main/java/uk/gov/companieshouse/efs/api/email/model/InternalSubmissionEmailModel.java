package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import java.util.Objects;

import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public class InternalSubmissionEmailModel {

    private final Submission submission;
    private final List<EmailFileDetails> emailFileDetailsList;

    public InternalSubmissionEmailModel(Submission submission, List<EmailFileDetails> emailFileDetailsList) {
        this.submission = submission;
        this.emailFileDetailsList = emailFileDetailsList;
    }

    public Submission getSubmission() {
        return submission;
    }

    public List<EmailFileDetails> getEmailFileDetailsList() {
        return emailFileDetailsList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailFileDetailsList, submission);
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
        InternalSubmissionEmailModel other = (InternalSubmissionEmailModel) obj;
        return Objects.equals(emailFileDetailsList, other.emailFileDetailsList)
                && Objects.equals(submission, other.submission);
    }




}
