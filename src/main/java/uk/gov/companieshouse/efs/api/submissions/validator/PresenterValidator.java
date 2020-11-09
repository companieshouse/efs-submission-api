package uk.gov.companieshouse.efs.api.submissions.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class PresenterValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (input.getPresenter() == null) {
            throw new SubmissionValidationException(String.format("Presenter details are absent in submission [%s]", input.getId()));
        } else if (StringUtils.isBlank(input.getPresenter().getEmail())) {
            throw new SubmissionValidationException(String.format("Presenter email is absent in submission [%s]", input.getId()));
        }
        super.validate(input);

    }
}
