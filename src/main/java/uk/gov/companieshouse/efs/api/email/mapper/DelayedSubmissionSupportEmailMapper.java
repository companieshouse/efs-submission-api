package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.DelayedSubmissionSupportEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class DelayedSubmissionSupportEmailMapper {
    private DelayedSubmissionSupportEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    /**
     * Constructor.
     *
     * @param config                dependency
     * @param idGenerator           dependency
     * @param timestampGenerator    dependency
     */
    public DelayedSubmissionSupportEmailMapper(DelayedSubmissionSupportEmailConfig config,
                                               IdentifierGeneratable idGenerator,
                                               TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<DelayedSubmissionSupportEmailData> map(DelayedSubmissionSupportEmailModel model) {
        return EmailDocument.<DelayedSubmissionSupportEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(config.getSupportEmailAddress())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromDelayedSubmissions(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private DelayedSubmissionSupportEmailData fromDelayedSubmissions(DelayedSubmissionSupportEmailModel model) {
        return DelayedSubmissionSupportEmailData.builder()
                .withTo(config.getSupportEmailAddress())
                .withSubject(config.getSubject())
                .withDelayedSubmissions(model.getDelayedSubmissions())
                .build();
    }
}
