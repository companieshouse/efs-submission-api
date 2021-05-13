package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.email.config.NotificationConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

public class ExternalNotificationEmailMapper {

    private NotificationConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    private CategoryTemplateService categoryTemplateService;
    private FormTemplateService formTemplateService;

    public ExternalNotificationEmailMapper(NotificationConfig config,
        IdentifierGeneratable idGenerator, TimestampGenerator<LocalDateTime> timestampGenerator,
        final CategoryTemplateService categoryTemplateService,
        final FormTemplateService formTemplateService) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.categoryTemplateService = categoryTemplateService;
        this.formTemplateService = formTemplateService;
    }

    public EmailDocument<ExternalConfirmationEmailData> map(ExternalNotificationEmailModel model) {
        return EmailDocument.<ExternalConfirmationEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(model.getSubmission().getPresenter().getEmail())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalConfirmationEmailData fromSubmission(ExternalNotificationEmailModel model) {
        return ExternalConfirmationEmailData.builder()
                .withTo(model.getSubmission().getPresenter().getEmail())
                .withPresenter(model.getSubmission().getPresenter())
                .withSubject(config.getSubject())
                .withCompany(model.getSubmission().getCompany())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withTopLevelCategory(getTopLevelCategoryForFormType(model))
                .withEmailFileDetailsList(createEmailFileDetailsList(model.getSubmission().getFormDetails().getFileDetailsList()))
                .withFeeOnSubmission(model.getSubmission().getFeeOnSubmission())
                .build();
    }

    private List<EmailFileDetails> createEmailFileDetailsList(final List<FileDetails> fileDetailsList) {
        return fileDetailsList.stream().map(this::emailFileDetails).collect(Collectors.toList());
    }

    private EmailFileDetails emailFileDetails(FileDetails fileDetails) {
        return new EmailFileDetails(fileDetails, null);
    }

    private CategoryTypeConstants getTopLevelCategoryForFormType(final ExternalNotificationEmailModel model) {
        FormTemplateApi formTemplate = formTemplateService
            .getFormTemplate(model.getSubmission().getFormDetails().getFormType());
        return categoryTemplateService.getTopLevelCategory(formTemplate.getFormCategory());
    }
}
