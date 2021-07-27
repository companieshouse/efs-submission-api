package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.Instant;
import java.time.ZoneId;
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
    private TimestampGenerator<Instant> timestampGenerator;
    private FormCategoryToEmailAddressService emailAddressService;

    @Autowired
    public InternalFailedConversionEmailMapper(InternalFailedConversionEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<Instant> timestampGenerator, FormCategoryToEmailAddressService emailAddressService) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
        this.emailAddressService = emailAddressService;
    }

    public EmailDocument<InternalFailedConversionEmailData> map(InternalFailedConversionModel model) {
        String emailAddress = emailAddressService.getEmailAddressForFormCategory(model.getSubmission().getFormDetails().getFormType());
        return EmailDocument.<InternalFailedConversionEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(emailAddress)
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model, emailAddress))
                .withCreatedAt(timestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private InternalFailedConversionEmailData fromSubmission(InternalFailedConversionModel model, String emailAddress) {
        return InternalFailedConversionEmailData.builder()
                .withTo(emailAddress)
                .withCompanyName(model.getSubmission().getCompany().getCompanyName())
                .withCompanyNumber(model.getSubmission().getCompany().getCompanyNumber())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withSubject(config.getSubject())
                .withFailedToConvert(model.getFailedToConvert())
                .withRejectionDate(model.getSubmission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .withUserEmail(model.getSubmission().getPresenter().getEmail())
                .build();
    }

}
