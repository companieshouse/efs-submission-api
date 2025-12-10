package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import uk.gov.companieshouse.efs.api.submissions.model.Submission;

/**
 * Represents an internal AV email failure.
 * @param submission the submission associated with the email
 * @param infectedFiles immutable list of infected files - if none, an empty list is set
 */
public record InternalAvFailedEmailModel(
    Submission submission,
    List<String> infectedFiles
) {
    public InternalAvFailedEmailModel {
        infectedFiles = List.copyOf(Objects.requireNonNullElse(infectedFiles, Collections.emptyList()));
    }
}
