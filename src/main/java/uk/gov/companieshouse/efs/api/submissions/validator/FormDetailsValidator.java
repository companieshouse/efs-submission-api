package uk.gov.companieshouse.efs.api.submissions.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class FormDetailsValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (input.getFormDetails() == null) {
            throw new SubmissionValidationException(
                "Form details are absent in submission [%s]".formatted(input.getId()));
        } else if (StringUtils.isBlank(input.getFormDetails().getFormType())) {
            throw new SubmissionValidationException(
                "Form type is absent in submission [%s]".formatted(input.getId()));
        }
        super.validate(input);
    }
}
