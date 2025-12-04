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
}
