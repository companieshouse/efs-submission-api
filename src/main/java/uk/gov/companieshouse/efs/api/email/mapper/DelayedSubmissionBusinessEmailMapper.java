package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.DelayedSubmissionBusinessEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class DelayedSubmissionBusinessEmailMapper {

    private DelayedSubmissionBusinessEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    /**
     * Constructor.
     *
     * @param config                dependency
     * @param idGenerator           dependency
     * @param timestampGenerator    dependency
     */
    @Autowired
    public DelayedSubmissionBusinessEmailMapper(final DelayedSubmissionBusinessEmailConfig config, final IdentifierGeneratable idGenerator, final TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<DelayedSubmissionBusinessEmailData> map(final DelayedSubmissionBusinessEmailModel model) {
        return EmailDocument.<DelayedSubmissionBusinessEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(model.getEmailAddress())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromDelayedSubmissions(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();

    }

    private DelayedSubmissionBusinessEmailData fromDelayedSubmissions(final DelayedSubmissionBusinessEmailModel model) {
        return new DelayedSubmissionBusinessEmailData(
            model.getEmailAddress(),
            config.getSubject(),
            model.getDelayedSubmissions(),
            Duration.ofHours(model.getDelayInHours()).toDays()
        );
    }
}
