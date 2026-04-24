package uk.gov.companieshouse.efs.api.submissions.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class CompanyDetailsValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (input.getCompany() == null) {
            throw new SubmissionValidationException(
                "Company details are absent in submission [%s]".formatted(input.getId()));
        } else if (StringUtils.isBlank(input.getCompany().getCompanyNumber())) {
            throw new SubmissionValidationException(
                "Company number is absent in submission [%s]".formatted(input.getId()));
        } else if (StringUtils.isBlank(input.getCompany().getCompanyName())) {
            throw new SubmissionValidationException(
                "Company name is absent in submission [%s]".formatted(input.getId()));
        }
        super.validate(input);

    }
}
