package uk.gov.companieshouse.efs.api.email.mapper;

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

import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.REGISTRAR_POWERS;

@Component
public class InternalSubmissionEmailMapper {

    private final InternalSubmissionEmailConfig internalSubmissionEmailConfig;
    private final IdentifierGeneratable idGenerator;
    private final TimestampGenerator<LocalDateTime> timestampGenerator;
    private final FormCategoryToEmailAddressService emailAddressService;
    private final CategoryTemplateService categoryTemplateService;
    private final FormTemplateService formTemplateService;

    public InternalSubmissionEmailMapper(final InternalSubmissionEmailConfig internalSubmissionEmailConfig, final IdentifierGeneratable idGenerator,
                                         final TimestampGenerator<LocalDateTime> timestampGenerator,
                                         final FormCategoryToEmailAddressService emailAddressService,
                                         final CategoryTemplateService categoryTemplateService,
                                         final FormTemplateService formTemplateService) {
        this.internalSubmissionEmailConfig = internalSubmissionEmailConfig;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.emailAddressService = emailAddressService;
        this.categoryTemplateService = categoryTemplateService;
        this.formTemplateService = formTemplateService;
    }

    public EmailDocument<InternalSubmissionEmailData> map(final InternalSubmissionEmailModel model) {

        final String emailAddress;
        final String formType = model.submission().getFormDetails().getFormType();
        final CategoryTypeConstants categoryType =
                categoryTemplateService.getTopLevelCategory(
                        formTemplateService.getFormTemplate(formType).getFormCategory());

        if (categoryType.getValue().equals(REGISTRAR_POWERS.getValue())) {

            final String companyNumber = model.submission().getCompany().getCompanyNumber();
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

    private InternalSubmissionEmailData fromSubmission(final InternalSubmissionEmailModel model, final String emailAddress) {
        return new InternalSubmissionEmailData(
            emailAddress,
            internalSubmissionEmailConfig.getSubject(),
            model.submission().getConfirmationReference(),
            model.submission().getPresenter(),
            model.submission().getCompany(),
            model.submission().getFormDetails().getFormType(),
            model.emailFileDetailsList()
        );
    }

}
