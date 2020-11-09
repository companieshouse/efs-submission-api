package uk.gov.companieshouse.efs.api.submissions.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class ConfirmationReferenceValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (StringUtils.isBlank(input.getConfirmationReference())) {
            throw new SubmissionValidationException(
                String.format("Confirmation reference is absent in submission [%s]", input.getId()));
        }
        super.validate(input);
    }
}
