package uk.gov.companieshouse.efs.api.submissions.validator;

import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public interface Validator<T> {
    void validate(T input) throws SubmissionValidationException;
}
