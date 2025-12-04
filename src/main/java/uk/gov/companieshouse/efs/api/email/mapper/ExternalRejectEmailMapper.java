package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.ExternalRejectedEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class ExternalRejectEmailMapper {
    private final ExternalRejectedEmailConfig config;
    private final IdentifierGeneratable idGenerator;
    private final TimestampGenerator<LocalDateTime> timestampGenerator;

    public ExternalRejectEmailMapper(ExternalRejectedEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<ExternalRejectEmailData> map(ExternalRejectEmailModel model) {
        return EmailDocument.<ExternalRejectEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(model.getSubmission().getPresenter().getEmail())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalRejectEmailData fromSubmission(ExternalRejectEmailModel model) {
        return new ExternalRejectEmailData(
            model.getSubmission().getPresenter().getEmail(),
            config.getSubject(),
            model.getSubmission().getCompany().getCompanyNumber(),
            model.getSubmission().getCompany().getCompanyName(),
            model.getSubmission().getConfirmationReference(),
            model.getSubmission().getFormDetails().getFormType(),
            model.getSubmission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())),
            model.getRejectReasons(),
            !Strings.isNullOrEmpty(model.getSubmission().getFeeOnSubmission())
        );
    }
}
