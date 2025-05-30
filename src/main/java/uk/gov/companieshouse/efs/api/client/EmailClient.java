package uk.gov.companieshouse.efs.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.efs.api.client.exception.EmailClientException;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class EmailClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final Supplier<InternalApiClient> internalApiClientSupplier;
    private final ObjectMapper objectMapper;

    public EmailClient(final Supplier<InternalApiClient> internalApiClientSupplier, final ObjectMapper objectMapper) {
        this.internalApiClientSupplier = internalApiClientSupplier;
        this.objectMapper = objectMapper;
    }

    public <T> ApiResponse<Void> sendEmail(final EmailDocument<T> document) throws EmailClientException {
        try {
            var jsonData = objectMapper.writeValueAsString(document.getData());

            var sendEmail = new SendEmail();
            sendEmail.setAppId(document.getAppId());
            sendEmail.setMessageId(document.getMessageId());
            sendEmail.setMessageType(document.getMessageType());
            sendEmail.setJsonData(jsonData);
            sendEmail.setEmailAddress(document.getEmailAddress());

            var requestId = getRequestId().orElse(UUID.randomUUID().toString());

            var apiClient = internalApiClientSupplier.get();
            apiClient.getHttpClient().setRequestId(requestId);

            var emailHandler = apiClient.sendEmailHandler();
            var emailPost = emailHandler.postSendEmail("/send-email", sendEmail);

            ApiResponse<Void> response = emailPost.execute();

            LOGGER.info(String.format("Posted '%s' email to CHS Kafka API (RequestId: %s): (Response %d)",
                    sendEmail.getMessageType(), apiClient.getHttpClient().getRequestId(), response.getStatusCode()));

            return response;

        } catch(JsonProcessingException ex) {
            LOGGER.error("Error creating payload", ex);
            throw new EmailClientException("Error creating payload for CHS Kafka API: ", ex);

        } catch (ApiErrorResponseException ex) {
            LOGGER.error("Error sending email", ex);
            throw new EmailClientException("Error sending payload to CHS Kafka API: ", ex);
        }
    }

    private Optional<String> getRequestId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(attributes.getRequest().getHeader("x-request-id"));
    }


}
