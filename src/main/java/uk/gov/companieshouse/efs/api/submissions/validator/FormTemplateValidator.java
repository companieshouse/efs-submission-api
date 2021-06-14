package uk.gov.companieshouse.efs.api.submissions.validator;

import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class FormTemplateValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    private final FormTemplateRepository repository;

    public FormTemplateValidator(final FormTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        if (repository != null && repository.findByIdFormType(input.getFormDetails().getFormType()).isEmpty()) {
            throw new SubmissionValidationException(String
                .format("Form type [%s] unknown in submission [%s]", input.getFormDetails().getFormType(),
                    input.getId()));
        }
        super.validate(input);
    }
}
