package uk.gov.companieshouse.efs.api.submissions.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class CompanyDetailsValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (input.getCompany() == null) {
            throw new SubmissionValidationException(
                String.format("Company details are absent in submission [%s]", input.getId()));
        } else if (StringUtils.isBlank(input.getCompany().getCompanyNumber())) {
            throw new SubmissionValidationException(
                String.format("Company number is absent in submission [%s]", input.getId()));
        } else if (StringUtils.isBlank(input.getCompany().getCompanyName())) {
            throw new SubmissionValidationException(
                String.format("Company name is absent in submission [%s]", input.getId()));
        }
        super.validate(input);

    }
}
