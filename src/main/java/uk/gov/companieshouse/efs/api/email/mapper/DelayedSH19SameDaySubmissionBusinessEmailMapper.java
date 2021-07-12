package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.DelayedSH19SameDaySubmissionBusinessEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class DelayedSH19SameDaySubmissionBusinessEmailMapper {
    private DelayedSH19SameDaySubmissionBusinessEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    /**
     * Constructor.
     *
     * @param config             dependency
     * @param idGenerator        dependency
     * @param timestampGenerator dependency
     */
    public DelayedSH19SameDaySubmissionBusinessEmailMapper(
        DelayedSH19SameDaySubmissionBusinessEmailConfig config, IdentifierGeneratable idGenerator,
        TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<DelayedSubmissionBusinessEmailData> map(
        DelayedSubmissionBusinessEmailModel model) {
        return EmailDocument.<DelayedSubmissionBusinessEmailData>builder()
            .withTopic(config.getTopic())
            .withMessageId(idGenerator.generateId())
            .withRecipientEmailAddress(model.getEmailAddress())
            .withEmailTemplateAppId(config.getAppId())
            .withEmailTemplateMessageType(config.getMessageType())
            .withData(fromDelayedSubmissions(model))
            .withCreatedAt(timestampGenerator.generateTimestamp()
                .format(DateTimeFormatter.ofPattern(config.getDateFormat())))
            .build();
    }

    private DelayedSubmissionBusinessEmailData fromDelayedSubmissions(
        DelayedSubmissionBusinessEmailModel model) {
        return DelayedSubmissionBusinessEmailData.builder()
            .withTo(model.getEmailAddress())
            .withSubject(config.getSubject())
            .withDelayedSubmissions(model.getDelayedSubmissions())
            .withThresholdInMinutes(model.getDelayInMinutes())
            .build();
    }
}
