package uk.gov.companieshouse.efs.api.email.model;

import java.util.List;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public record InternalFailedConversionModel(Submission submission, List<String> failedToConvert) {}
