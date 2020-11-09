package uk.gov.companieshouse.efs.api.submissions.validator;

import java.util.Optional;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class ConfirmAuthorisedValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    private final FormTemplateRepository formRepository;
    private final CategoryTemplateService categoryService;

    public ConfirmAuthorisedValidator(final FormTemplateRepository formRepository,
        final CategoryTemplateService categoryService) {
        this.formRepository = formRepository;
        this.categoryService = categoryService;
    }

    @Override
    public void validate(final Submission input) throws SubmissionValidationException {
        final Optional<FormTemplate> template = formRepository.findById(input.getFormDetails().getFormType());
        final Optional<String> category = template.map(FormTemplate::getFormCategory);
        if (categoryService != null) {
            final CategoryTypeConstants topLevelCategory =
                category.map(categoryService::getTopLevelCategory).orElse(CategoryTypeConstants.OTHER);
            if (CategoryTypeConstants.INSOLVENCY.equals(topLevelCategory) && !Boolean.TRUE
                .equals(input.getConfirmAuthorised())) {

                throw new SubmissionValidationException(String
                    .format("Presenter must confirm they are authorised in submission [%s] for Insolvency form [%s]",
                        input.getId(), template.map(FormTemplate::getFormType).orElse(null)));
            }
        }
        super.validate(input);
    }
}
