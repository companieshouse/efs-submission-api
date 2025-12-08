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
                .withRecipientEmailAddress(model.submission().getPresenter().getEmail())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalRejectEmailData fromSubmission(ExternalRejectEmailModel model) {
        return ExternalRejectEmailData.builder()
                .withTo(model.submission().getPresenter().getEmail())
                .withSubject(config.getSubject())
                .withCompanyNumber(model.submission().getCompany().getCompanyNumber())
                .withCompanyName(model.submission().getCompany().getCompanyName())
                .withConfirmationReference(model.submission().getConfirmationReference())
                .withFormType(model.submission().getFormDetails().getFormType())
                .withRejectionDate(model.submission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .withRejectReasons(model.rejectReasons())
                .withIsPaidForm(!Strings.isNullOrEmpty(model.submission().getFeeOnSubmission()))
                .build();
    }
}
