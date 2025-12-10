package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;

public record DelayedSubmissionBusinessEmailData(
    String to,
    String subject,
    List<DelayedSubmissionBusinessModel> submissions,
    long delayInDays
) {}
