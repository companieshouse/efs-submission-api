package uk.gov.companieshouse.efs.api.email.mapper;

import com.google.common.base.Strings;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.ExternalRejectedEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class ExternalRejectEmailMapper {
    private ExternalRejectedEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<Instant> timestampGenerator;

    public ExternalRejectEmailMapper(ExternalRejectedEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<Instant> timestampGenerator) {
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
                .withCreatedAt(timestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalRejectEmailData fromSubmission(ExternalRejectEmailModel model) {
        return ExternalRejectEmailData.builder()
                .withTo(model.getSubmission().getPresenter().getEmail())
                .withSubject(config.getSubject())
                .withCompanyNumber(model.getSubmission().getCompany().getCompanyNumber())
                .withCompanyName(model.getSubmission().getCompany().getCompanyName())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withRejectionDate(model.getSubmission().getLastModifiedAt().format(DateTimeFormatter.ofPattern(config.getDateFormat())))
                .withRejectReasons(model.getRejectReasons())
                .withIsPaidForm(!Strings.isNullOrEmpty(model.getSubmission().getFeeOnSubmission()))
                .build();
    }
}
