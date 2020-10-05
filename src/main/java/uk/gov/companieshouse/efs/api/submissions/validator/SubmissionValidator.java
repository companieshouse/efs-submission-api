package uk.gov.companieshouse.efs.api.submissions.validator;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class SubmissionValidator implements Validator<Submission> {

    private FormTemplateRepository formRepository;
    private CategoryTemplateService categoryTemplateService;
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");


    @Autowired
    public SubmissionValidator(FormTemplateRepository formRepository, CategoryTemplateService categoryTemplateService) {
        this.formRepository = formRepository;
        this.categoryTemplateService = categoryTemplateService;
    }

    @Override
    public void validate(Submission input) throws SubmissionValidationException {

        LOGGER.info(String.format("About to validate submission with id: [%s]", input.getId()));

        if (input.getPresenter() == null) {
            throw new SubmissionValidationException(String.format("Presenter details are absent in submission [%s]", input.getId()));
        } else if (isBlank(input.getPresenter().getEmail())) {
            throw new SubmissionValidationException(String.format("Presenter email is absent in submission [%s]", input.getId()));
        } else if (input.getCompany() == null) {
            throw new SubmissionValidationException(String.format("Company details are absent in submission [%s]", input.getId()));
        } else if (isBlank(input.getCompany().getCompanyNumber())) {
            throw new SubmissionValidationException(String.format("Company number is absent in submission [%s]", input.getId()));
        } else if (isBlank(input.getCompany().getCompanyName())) {
            throw new SubmissionValidationException(String.format("Company name is absent in submission [%s]", input.getId()));
        } else if (input.getFormDetails() == null) {
            throw new SubmissionValidationException(String.format("Form details are absent in submission [%s]", input.getId()));
        } else if (isBlank(input.getFormDetails().getFormType())) {
            throw new SubmissionValidationException(String.format("Form type is absent in submission [%s]", input.getId()));
        } else if (isBlank(input.getConfirmationReference())) {
            throw new SubmissionValidationException(String.format("Confirmation reference is absent in submission [%s]", input.getId()));
        }

        Optional<FormTemplate> form = formRepository.findById(input.getFormDetails().getFormType());

        FormTemplate theForm = form.orElseThrow(() -> new SubmissionValidationException(String
                .format("Form type [%s] unknown in submission [%s]", input.getFormDetails().getFormType(), input.getId())));

        if (input.getFormDetails().getFileDetailsList() == null) {
            throw new SubmissionValidationException(String.format("File details are absent in submission [%s]", input.getId()));
        } else if (input.getFormDetails().getFileDetailsList().isEmpty()) {
            throw new SubmissionValidationException(String.format("File details are empty in submission [%s]", input.getId()));
        } else if (input.getFormDetails().getFileDetailsList().contains(null)) {
            throw new SubmissionValidationException(
                    String.format("File details contains null in submission [%s]", input.getId()));
        }
        if (isPaymentRequired(theForm) && isBlank(input.getPaymentReference())) {
            throw new SubmissionValidationException(
                    String.format("Payment reference is absent for fee paying form [%s] in submission [%s]",
                    theForm.getFormType(), input.getId()));
        } else if (!isPaymentRequired(theForm) && !isBlank(input.getPaymentReference())) {
            throw new SubmissionValidationException(
                    String.format("Payment reference is present for the non fee paying form [%s] in submission [%s]",
                    theForm.getFormType(), input.getId()));
        } else if (theForm.isFesEnabled() && input.getFormDetails().getFileDetailsList().size() > 1) {
            throw new SubmissionValidationException(String.format("Attachments present in submission [%s] for FES enabled form [%s]",
                    input.getId(), theForm.getFormType()));
        }
        if ((input.getConfirmAuthorised() == null || !input.getConfirmAuthorised().equals(true))
            && categoryTemplateService.getTopLevelCategory(theForm.getFormCategory())
            == CategoryTypeConstants.INSOLVENCY) {
            throw new SubmissionValidationException(String
                .format("Presenter must confirm they are authorised in submission [%s] for Insolvency form [%s]",
                    input.getId(), theForm.getFormType()));
        }

        LOGGER.info(String.format("Successfully validated submission with id: [%s]", input.getId()));

    }

    private boolean isBlank(String info) {
        return info == null || info.isEmpty();
    }

    private boolean isPaymentRequired(final FormTemplate form) {
        final String fee = form.getFee();
        final BigDecimal feeAmount = StringUtils.isNotBlank(fee)
                ? new BigDecimal(fee)
                : BigDecimal.ZERO;

        return feeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

}
