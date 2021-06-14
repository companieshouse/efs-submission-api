package uk.gov.companieshouse.efs.api.email.mapper;

import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.REGISTRAR_POWERS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.InternalSubmissionEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class InternalSubmissionEmailMapper {

    private InternalSubmissionEmailConfig internalSubmissionEmailConfig;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    private FormCategoryToEmailAddressService emailAddressService;
    private CategoryTemplateService categoryTemplateService;
    private FormTemplateService formTemplateService;

    public InternalSubmissionEmailMapper(InternalSubmissionEmailConfig internalSubmissionEmailConfig, IdentifierGeneratable idGenerator,
                                         TimestampGenerator<LocalDateTime> timestampGenerator,
                                         FormCategoryToEmailAddressService emailAddressService,
                                         CategoryTemplateService categoryTemplateService,
                                         FormTemplateService formTemplateService) {
        this.internalSubmissionEmailConfig = internalSubmissionEmailConfig;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.emailAddressService = emailAddressService;
        this.categoryTemplateService = categoryTemplateService;
        this.formTemplateService = formTemplateService;
    }

    public EmailDocument<InternalSubmissionEmailData> map(InternalSubmissionEmailModel model) {

        String emailAddress;
        String formType = model.getSubmission().getFormDetails().getFormType();
        CategoryTypeConstants categoryType = categoryTemplateService.getTopLevelCategory(
            formTemplateService.getFormTemplate(formType).getFormCategory());

        if (categoryType.getValue().equals(REGISTRAR_POWERS.getValue())) {

            String companyNumber = model.getSubmission().getCompany().getCompanyNumber();
            emailAddress = emailAddressService.getEmailAddressForRegPowersFormCategory(formType, companyNumber);

        } else {
            emailAddress = emailAddressService.getEmailAddressForFormCategory(formType);
        }
        return EmailDocument.<InternalSubmissionEmailData>builder().withTopic(internalSubmissionEmailConfig.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(emailAddress)
                .withEmailTemplateAppId(internalSubmissionEmailConfig.getAppId())
                .withEmailTemplateMessageType(internalSubmissionEmailConfig.getMessageType())
                .withData(fromSubmission(model, emailAddress))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(internalSubmissionEmailConfig.getDateFormat()))).build();
    }

    private InternalSubmissionEmailData fromSubmission(InternalSubmissionEmailModel model, String emailAddress) {
        return InternalSubmissionEmailData.builder()
                .withTo(emailAddress)
                .withPresenter(model.getSubmission().getPresenter()).withSubject(internalSubmissionEmailConfig.getSubject())
                .withCompany(model.getSubmission().getCompany())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withEmailFileDetailsList(model.getEmailFileDetailsList())
                .build();
    }

}
