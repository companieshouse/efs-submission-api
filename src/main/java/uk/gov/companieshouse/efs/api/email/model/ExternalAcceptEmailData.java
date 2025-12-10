package uk.gov.companieshouse.efs.api.email.model;

/**
 * Represents the external accept email data for notification purposes.
 * @param to recipient email address
 * @param subject email subject
 * @param companyNumber company number
 * @param companyName company name
 * @param confirmationReference confirmation reference
 * @param formType form type
 * @param submittedDate date of submission
 */
public record ExternalAcceptEmailData(
    String to,
    String subject,
    String companyNumber,
    String companyName,
    String confirmationReference,
    String formType,
    String submittedDate
) {}
