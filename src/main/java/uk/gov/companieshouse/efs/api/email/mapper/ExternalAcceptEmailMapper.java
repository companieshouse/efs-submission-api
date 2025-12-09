package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.ExternalAcceptedEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class ExternalAcceptEmailMapper {
    private ExternalAcceptedEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    public ExternalAcceptEmailMapper(ExternalAcceptedEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<ExternalAcceptEmailData> map(ExternalAcceptEmailModel model) {
        return EmailDocument.<ExternalAcceptEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(model.getSubmission().getPresenter().getEmail())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalAcceptEmailData fromSubmission(ExternalAcceptEmailModel model) {
        LocalDateTime submittedAt = model.getSubmission().getSubmittedAt() == null ? model.getSubmission().getCreatedAt() : model.getSubmission().getSubmittedAt();
        return new ExternalAcceptEmailData(
            model.getSubmission().getPresenter().getEmail(),
            config.getSubject(),
            model.getSubmission().getCompany().getCompanyNumber(),
            model.getSubmission().getCompany().getCompanyName(),
            model.getSubmission().getConfirmationReference(),
            model.getSubmission().getFormDetails().getFormType(),
            submittedAt.format(DateTimeFormatter.ofPattern(config.getDateFormat()))
        );
    }

}
