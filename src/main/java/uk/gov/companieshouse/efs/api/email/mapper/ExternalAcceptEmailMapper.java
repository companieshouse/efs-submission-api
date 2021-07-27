package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
    private TimestampGenerator<Instant> timestampGenerator;

    public ExternalAcceptEmailMapper(ExternalAcceptedEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<Instant> timestampGenerator) {
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
                .withCreatedAt(timestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalAcceptEmailData fromSubmission(ExternalAcceptEmailModel model) {
        LocalDateTime submittedAt = Optional.ofNullable(model.getSubmission().getSubmittedAt()).orElse(Optional.ofNullable(model.getSubmission().getCreatedAt()).map(i -> i.atZone(ZoneId.of("UTC")).toLocalDateTime()).orElse(null));
        return ExternalAcceptEmailData.builder()
                .withTo(model.getSubmission().getPresenter().getEmail())
                .withSubject(config.getSubject())
                .withCompanyNumber(model.getSubmission().getCompany().getCompanyNumber())
                .withCompanyName(model.getSubmission().getCompany().getCompanyName())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withSubmittedDate(submittedAt.format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .build();
    }

}
