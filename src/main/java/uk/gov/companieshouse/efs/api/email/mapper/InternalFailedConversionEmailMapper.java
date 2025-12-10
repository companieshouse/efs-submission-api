package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.InternalFailedConversionEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class InternalFailedConversionEmailMapper {

    private InternalFailedConversionEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    private FormCategoryToEmailAddressService emailAddressService;

    @Autowired
    public InternalFailedConversionEmailMapper(final InternalFailedConversionEmailConfig config, final IdentifierGeneratable idGenerator, final TimestampGenerator<LocalDateTime> timestampGenerator, final FormCategoryToEmailAddressService emailAddressService) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.emailAddressService = emailAddressService;
    }

    public EmailDocument<InternalFailedConversionEmailData> map(final InternalFailedConversionModel model) {
        final String emailAddress = emailAddressService.getEmailAddressForFormCategory(model.submission().getFormDetails().getFormType());
        return EmailDocument.<InternalFailedConversionEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(emailAddress)
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model, emailAddress))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private InternalFailedConversionEmailData fromSubmission(final InternalFailedConversionModel model, final String emailAddress) {
        return InternalFailedConversionEmailData.builder()
                .withTo(emailAddress)
                .withCompanyName(model.submission().getCompany().getCompanyName())
                .withCompanyNumber(model.submission().getCompany().getCompanyNumber())
                .withConfirmationReference(model.submission().getConfirmationReference())
                .withFormType(model.submission().getFormDetails().getFormType())
                .withSubject(config.getSubject())
                .withFailedToConvert(model.failedToConvert())
                .withRejectionDate(model.submission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .withUserEmail(model.submission().getPresenter().getEmail())
                .build();
    }

}
