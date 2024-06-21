package uk.gov.companieshouse.efs.api.email;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;
import uk.gov.companieshouse.efs.api.kafka.CHKafkaProducer;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class EmailServiceImpl implements EmailService {

    private CHKafkaProducer producer;
    private EmailSerialiser serializer;
    private Schema schema;
    private EmailMapperFactory emailMapperFactory;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    /**
     * Constructor.
     *
     * @param producer              dependency
     * @param serializer            dependency
     * @param schema                dependency
     * @param emailMapperFactory    dependency
     * @param timestampGenerator    dependency
     */
    @Autowired
    public EmailServiceImpl(CHKafkaProducer producer, EmailSerialiser serializer, Schema schema,
            EmailMapperFactory emailMapperFactory, TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.producer = producer;
        this.serializer = serializer;
        this.schema = schema;
        this.emailMapperFactory = emailMapperFactory;
        this.timestampGenerator = timestampGenerator;
    }

    @Override
    public void sendExternalConfirmation(ExternalNotificationEmailModel emailModel) {
        LOGGER.debug(String.format("Sending external email confirming submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getConfirmationEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalPaymentFailedNotification(ExternalNotificationEmailModel emailModel) {
        LOGGER.debug(String.format("Sending external email notifying payment failed for submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getPaymentFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalAccept(ExternalAcceptEmailModel emailModel) {
        LOGGER.debug(String.format("Sending external email accepting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getAcceptEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalReject(ExternalRejectEmailModel emailModel) {
        LOGGER.debug(String.format("Sending external email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getRejectEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedAV(InternalAvFailedEmailModel emailModel) {
        LOGGER.debug(String.format("Sending internal av failed email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalAvFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalSubmission(InternalSubmissionEmailModel emailModel) {
        LOGGER.debug(String.format("Sending submission [%s] to internal email", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalSubmissionEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedConversion(InternalFailedConversionModel emailModel) {
        LOGGER.debug(String.format("Sending internal failed conversion email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalFailedConversionEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSubmissionSupportEmail(DelayedSubmissionSupportEmailModel emailModel) {
        LOGGER.debug(String.format("Sending delayed submission support email for [%d] submissions",
                emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionSupportEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSH19SubmissionSupportEmail(DelayedSubmissionSupportEmailModel emailModel,
        String businessEmail) {
        LOGGER.debug(String.format(
            "Sending delayed SH19 same day submission support email for [%d] submissions",
            emailModel.getNumberOfDelayedSubmissions()));
        final EmailDocument<DelayedSubmissionSupportEmailData> document =
            this.emailMapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper()
                .map(emailModel);
        sendMessage(document);
        
        final DelayedSubmissionSupportEmailData data = document.getData();
        
        data.setTo(businessEmail);

        LOGGER.debug(String.format(
            "Sending delayed SH19 same day submission business email for [%d] submissions",
            emailModel.getNumberOfDelayedSubmissions()));
        final EmailDocument<DelayedSubmissionSupportEmailData> businessCopy =
            new EmailDocument<>(document.getAppId(), document.getMessageId(),
                document.getMessageType(), data, businessEmail,
                document.getCreatedAt(), document.getTopic());
        sendMessage(businessCopy);
    }

    @Override
    public void sendDelayedSubmissionBusinessEmail(DelayedSubmissionBusinessEmailModel emailModel) {
        LOGGER.debug(String.format("Sending delayed submission business email for [%d] submissions",
                emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionBusinessEmailMapper().map(emailModel));
    }

    @Override
    public void sendPaymentReportEmail(PaymentReportEmailModel emailModel) {
        LOGGER.debug("Sending payment report email");
        sendMessage(this.emailMapperFactory.getPaymentReportEmailMapper().map(emailModel));
    }

    private void sendMessage(EmailDocument<?> document) {
        Message result = new Message();
        result.setTopic(document.getTopic());
        result.setTimestamp(timestampGenerator.generateTimestamp().toEpochSecond(ZoneOffset.UTC));
        try {
            result.setValue(serializer.serialize(document, schema));
            producer.send(result);
        } catch (ExecutionException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("document", document);
            errorMap.put("message", result);
            LOGGER.errorContext(document.getAppId(), ex, errorMap);
            throw new EmailServiceException("Error sending message to kafka", ex);
        } catch (InterruptedException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("document", document);
            errorMap.put("message", result);
            LOGGER.errorContext(document.getAppId(), ex, errorMap);
            Thread.currentThread().interrupt();
            throw new EmailServiceException("Error - thread interrupted", ex);
        }
    }
}
