package uk.gov.companieshouse.efs.api.email.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

/**
 * Represents the external reject email model.
 * @param submission the submission associated with the email
 * @param rejectReasons immutable list of reject reasons (empty list if no rejectReasons provided)
 */
public record ExternalRejectEmailModel(
    Submission submission,
    List<String> rejectReasons
) {
    public ExternalRejectEmailModel {
        rejectReasons = List.copyOf(Objects.requireNonNullElse(rejectReasons, Collections.emptyList()));
    }
}
