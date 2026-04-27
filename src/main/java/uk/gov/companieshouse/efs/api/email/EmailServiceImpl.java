package uk.gov.companieshouse.efs.api.email;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.efs.api.client.EmailClient;
import uk.gov.companieshouse.efs.api.client.exception.EmailClientException;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Service implementation for sending various types of email notifications related to submissions.
 * <p>
 * This class coordinates the mapping of submission models to email documents and delegates the actual
 * sending of emails to the {@link EmailClient}. It supports external and internal notifications, payment
 * reports, delayed submission notifications, and more. Each public method corresponds to a specific type
 * of email notification and uses the appropriate mapper from {@link EmailMapperFactory}.
 * <p>
 * Error handling is performed for failed email sends, and all operations are logged for traceability.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final EmailMapperFactory emailMapperFactory;
    private final EmailClient emailClient;

    /**
     * Constructor.
     *
     * @param emailMapperFactory    dependency
     * @param emailClient           dependency
     */
    public EmailServiceImpl(final EmailMapperFactory emailMapperFactory, final EmailClient emailClient) {
        this.emailMapperFactory = emailMapperFactory;
        this.emailClient = emailClient;
    }

    @Override
    public void sendExternalConfirmation(final ExternalNotificationEmailModel emailModel) {
        LOGGER.debug("Sending external email confirming submission [%s]".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getConfirmationEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalPaymentFailedNotification(final ExternalNotificationEmailModel emailModel) {
        LOGGER.debug("Sending external email notifying payment failed for submission [%s]".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getPaymentFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalAccept(final ExternalAcceptEmailModel emailModel) {
        LOGGER.debug("Sending external email accepting submission [%s]".formatted(emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getAcceptEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalReject(final ExternalRejectEmailModel emailModel) {
        LOGGER.debug("Sending external email rejecting submission [%s]".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getRejectEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedAV(final InternalAvFailedEmailModel emailModel) {
        LOGGER.debug("Sending internal av failed email rejecting submission [%s]".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getInternalAvFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalSubmission(final InternalSubmissionEmailModel emailModel) {
        LOGGER.debug("Sending submission [%s] to internal email".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getInternalSubmissionEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedConversion(final InternalFailedConversionModel emailModel) {
        LOGGER.debug("Sending internal failed conversion email rejecting submission [%s]".formatted(emailModel.submission().getId()));
        sendMessage(this.emailMapperFactory.getInternalFailedConversionEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSubmissionSupportEmail(final DelayedSubmissionSupportEmailModel emailModel) {
        LOGGER.debug("Sending delayed submission support email for [%d] submissions".formatted(
            emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionSupportEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSH19SubmissionSupportEmail(final DelayedSubmissionSupportEmailModel emailModel,
                                                      final String businessEmail) {
        LOGGER.debug("Sending delayed SH19 same day submission support email for [%d] submissions".formatted(
            emailModel.getNumberOfDelayedSubmissions()));
        final var document = this.emailMapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper().map(
            emailModel);
        sendMessage(document);
        
        final var data = document.getData();
        
        data.setTo(businessEmail);

        LOGGER.debug("Sending delayed SH19 same day submission business email for [%d] submissions".formatted(
            emailModel.getNumberOfDelayedSubmissions()));
        final var businessCopy = new EmailDocument<>(document.getAppId(), document.getMessageId(),
            document.getMessageType(), data, businessEmail, document.getCreatedAt(), document.getTopic());
        sendMessage(businessCopy);
    }

    @Override
    public void sendDelayedSubmissionBusinessEmail(final DelayedSubmissionBusinessEmailModel emailModel) {
        LOGGER.debug("Sending delayed submission business email for [%d] submissions".formatted(
            emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionBusinessEmailMapper().map(emailModel));
    }

    @Override
    public void sendPaymentReportEmail(final PaymentReportEmailModel emailModel) {
        LOGGER.debug("Sending payment report email");
        sendMessage(this.emailMapperFactory.getPaymentReportEmailMapper().map(emailModel));
    }

    private void sendMessage(final EmailDocument<?> document) {
        LOGGER.debug("Sending message to [%s]".formatted(document.getEmailAddress()));
        try {
            final var response = emailClient.sendEmail(document);

            if (response.getStatusCode() != HttpStatus.OK.value()) {
                LOGGER.error("Error sending document to email client: [%d]".formatted(response.getStatusCode()));

                final var errorList = response.getErrors().stream().map(ApiError::getError).collect(Collectors.joining());
                throw new EmailServiceException("Error sending request to CHS Kafka API: %s".formatted(errorList));
            }

        } catch(final EmailClientException ex) {
            final Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("document", document);
            LOGGER.errorContext(document.getAppId(), ex, errorMap);

            throw new EmailServiceException("Error sending document to email client: ", ex);
        }
    }
}
