package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public record InternalSubmissionEmailModel(
    Submission submission,
    List<EmailFileDetails> emailFileDetailsList
) {
    public InternalSubmissionEmailModel {
        emailFileDetailsList = emailFileDetailsList == null ? List.of() : List.copyOf(emailFileDetailsList);
    }
}
