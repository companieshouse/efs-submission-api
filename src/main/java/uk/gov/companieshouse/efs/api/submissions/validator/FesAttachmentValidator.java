package uk.gov.companieshouse.efs.api.submissions.validator;

import java.util.Optional;
import org.springframework.util.CollectionUtils;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;

public class FesAttachmentValidator extends ValidatorImpl<Submission> implements Validator<Submission> {
    private final FormTemplateRepository formRepository;

    public FesAttachmentValidator(final FormTemplateRepository formRepository) {
        this.formRepository = formRepository;
    }

    @Override
    public void validate(final Submission input) throws SubmissionValidationException {

        if (formRepository != null) {
            final Optional<FormTemplate> template = formRepository.findById(input.getFormDetails().getFormType());
            final boolean hasAttachments = !CollectionUtils.isEmpty(input.getFormDetails().getFileDetailsList());

            // don't throw if !template.isPresent()
            if (template.isPresent() && template.filter(FormTemplate::isFesEnabled).isPresent() && hasAttachments) {
                throw new SubmissionValidationException(String
                    .format("Attachments present in submission [%s] for FES enabled form [%s]", input.getId(),
                        template.get().getFormType()));
            }
        }
        super.validate(input);
    }
}
