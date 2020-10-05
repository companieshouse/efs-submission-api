package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.InternalFailedAvEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class InternalAvFailedEmailMapper {
    private InternalFailedAvEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    private FormCategoryToEmailAddressService emailAddressService;

    /**
     * Constructor.
     *
     * @param config                dependency
     * @param idGenerator           dependency
     * @param timestampGenerator    dependency
     * @param emailAddressService   dependency
     */
    public InternalAvFailedEmailMapper(InternalFailedAvEmailConfig config, IdentifierGeneratable idGenerator,
                                       TimestampGenerator<LocalDateTime> timestampGenerator,
                                       FormCategoryToEmailAddressService emailAddressService) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.emailAddressService = emailAddressService;
    }

    /**
     * Maps model data to EmailDocument.
     *
     * @param model     the email model
     * @return          EmailDocument&lt;InternalAvFailedEmailData&gt;
     */
    public EmailDocument<InternalAvFailedEmailData> map(InternalAvFailedEmailModel model) {
        String emailAddress = emailAddressService.getEmailAddressForFormCategory(model.getSubmission().getFormDetails().getFormType());
        return EmailDocument.<InternalAvFailedEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(emailAddress)
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model, emailAddress))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private InternalAvFailedEmailData fromSubmission(InternalAvFailedEmailModel model, String emailAddress) {
        return InternalAvFailedEmailData.builder()
                .withTo(emailAddress)
                .withCompanyName(model.getSubmission().getCompany().getCompanyName())
                .withCompanyNumber(model.getSubmission().getCompany().getCompanyNumber())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withSubject(config.getSubject())
                .withInfectedFiles(model.getInfectedFiles())
                .withRejectionDate(model.getSubmission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .withUserEmail(model.getSubmission().getPresenter().getEmail())
                .build();
    }
}
