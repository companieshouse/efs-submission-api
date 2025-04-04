package uk.gov.companieshouse.efs.api.email;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.efs.api.client.EmailClient;
import uk.gov.companieshouse.efs.api.client.exception.EmailClientException;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.model.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

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
    public EmailServiceImpl(EmailMapperFactory emailMapperFactory, EmailClient emailClient) {
        this.emailMapperFactory = emailMapperFactory;
        this.emailClient = emailClient;
    }

    @Override
    public void sendExternalConfirmation(ExternalNotificationEmailModel emailModel) {
        LOGGER.debug(format("Sending external email confirming submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getConfirmationEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalPaymentFailedNotification(ExternalNotificationEmailModel emailModel) {
        LOGGER.debug(format("Sending external email notifying payment failed for submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getPaymentFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalAccept(ExternalAcceptEmailModel emailModel) {
        LOGGER.debug(format("Sending external email accepting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getAcceptEmailMapper().map(emailModel));
    }

    @Override
    public void sendExternalReject(ExternalRejectEmailModel emailModel) {
        LOGGER.debug(format("Sending external email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getRejectEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedAV(InternalAvFailedEmailModel emailModel) {
        LOGGER.debug(format("Sending internal av failed email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalAvFailedEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalSubmission(InternalSubmissionEmailModel emailModel) {
        LOGGER.debug(format("Sending submission [%s] to internal email", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalSubmissionEmailMapper().map(emailModel));
    }

    @Override
    public void sendInternalFailedConversion(InternalFailedConversionModel emailModel) {
        LOGGER.debug(format("Sending internal failed conversion email rejecting submission [%s]", emailModel.getSubmission().getId()));
        sendMessage(this.emailMapperFactory.getInternalFailedConversionEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSubmissionSupportEmail(DelayedSubmissionSupportEmailModel emailModel) {
        LOGGER.debug(format("Sending delayed submission support email for [%d] submissions",
                emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionSupportEmailMapper().map(emailModel));
    }

    @Override
    public void sendDelayedSH19SubmissionSupportEmail(DelayedSubmissionSupportEmailModel emailModel,
        String businessEmail) {
        LOGGER.debug(format(
            "Sending delayed SH19 same day submission support email for [%d] submissions",
            emailModel.getNumberOfDelayedSubmissions()));
        final EmailDocument<DelayedSubmissionSupportEmailData> document =
            this.emailMapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper()
                .map(emailModel);
        sendMessage(document);
        
        final DelayedSubmissionSupportEmailData data = document.getData();
        
        data.setTo(businessEmail);

        LOGGER.debug(format(
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
        LOGGER.debug(format("Sending delayed submission business email for [%d] submissions",
                emailModel.getNumberOfDelayedSubmissions()));
        sendMessage(this.emailMapperFactory.getDelayedSubmissionBusinessEmailMapper().map(emailModel));
    }

    @Override
    public void sendPaymentReportEmail(PaymentReportEmailModel emailModel) {
        LOGGER.debug("Sending payment report email");
        sendMessage(this.emailMapperFactory.getPaymentReportEmailMapper().map(emailModel));
    }

    private void sendMessage(final EmailDocument<?> document) {
        LOGGER.debug(format("Sending message to [%s]", document.getEmailAddress()));
        try {
            ApiResponse<Void> response = emailClient.sendEmail(document);

            if (response.getStatusCode() != HttpStatus.OK.value()) {
                LOGGER.error(format("Error sending document to email client: [%d]", response.getStatusCode()));

                String errorList = response.getErrors().stream().map(ApiError::getError).collect(Collectors.joining());
                throw new EmailServiceException(format("Error sending request to CHS Kafka API: %s", errorList));
            }

        } catch(EmailClientException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("document", document);
            LOGGER.errorContext(document.getAppId(), ex, errorMap);

            throw new EmailServiceException("Error sending document to email client: ", ex);
        }
    }
}
