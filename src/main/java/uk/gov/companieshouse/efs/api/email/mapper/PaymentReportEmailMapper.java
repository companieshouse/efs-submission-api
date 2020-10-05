package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.email.config.PaymentReportEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailData;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class PaymentReportEmailMapper {

    private PaymentReportEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Autowired
    public PaymentReportEmailMapper(final PaymentReportEmailConfig config, IdentifierGeneratable idGenerator, TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<PaymentReportEmailData> map(PaymentReportEmailModel model) {
        return EmailDocument.<PaymentReportEmailData>builder()
            .withTopic(config.getTopic())
            .withMessageId(idGenerator.generateId())
            .withRecipientEmailAddress(config.getFinanceEmailAddress())
            .withEmailTemplateAppId(config.getAppId())
            .withEmailTemplateMessageType(config.getMessageType())
            .withData(fromPaymentReport(model))
            .withCreatedAt(timestampGenerator.generateTimestamp()
                .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private PaymentReportEmailData fromPaymentReport(PaymentReportEmailModel model) {
        return PaymentReportEmailData.builder()
            .withTo(model.getFileName().contains("Scottish") ? config.getScottishEmailAddress() : config.getFinanceEmailAddress())
            .withSubject(model.getFileName())
            .withFileLink(model.getFileLink())
            .withFileName(model.getFileName())
            .witHasNoPaymentTransactions(model.getHasNoPaymentTransactions())
            .build();
    }
}