package uk.gov.companieshouse.efs.api.email.model;

import uk.gov.companieshouse.efs.api.submissions.model.Submission;

/**
 * Represents an external notification email model containing a submission.
 */
public record ExternalNotificationEmailModel(Submission submission) {}
