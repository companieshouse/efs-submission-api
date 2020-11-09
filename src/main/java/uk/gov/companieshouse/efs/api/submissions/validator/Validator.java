package uk.gov.companieshouse.efs.api.submissions.validator;

import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public interface Validator<T> {
    Validator<T> setNext(Validator<T> next);

    void validate(T input) throws SubmissionValidationException;
}
