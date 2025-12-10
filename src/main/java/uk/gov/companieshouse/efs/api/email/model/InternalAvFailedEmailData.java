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

}
