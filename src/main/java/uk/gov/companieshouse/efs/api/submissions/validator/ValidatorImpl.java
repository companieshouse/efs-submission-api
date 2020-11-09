package uk.gov.companieshouse.efs.api.submissions.validator;

import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public abstract class ValidatorImpl<T> implements Validator<T> {
    private Validator<T> next;

    @Override
    public Validator<T> setNext(final Validator<T> next) {
        this.next = next;
        return next;
    }

    @Override
    public void validate(final T input) throws SubmissionValidationException {
        if (next != null) {
            next.validate(input);
        }
    }
}
