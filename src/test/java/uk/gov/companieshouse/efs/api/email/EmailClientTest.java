package uk.gov.companieshouse.efs.api.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateSendEmailPost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.efs.api.client.EmailClient;
import uk.gov.companieshouse.efs.api.client.exception.EmailClientException;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailData;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailClientTest {

    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;

    @Mock
    private InternalApiClient internalApiClient;

    @InjectMocks
    private EmailClient emailClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailClient, "objectMapper", new ObjectMapper());
    }

    @Test
    void givenValidPayload_whenEmailRequested_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        PaymentReportEmailData emailData = new PaymentReportEmailData("unit@test.com", "My Payment Subject", "file://file-link", "filename.pdf", false);
        EmailDocument<PaymentReportEmailData> document = getPaymentEmailDocument(emailData);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenInvalidPayload_whenEmailRequested_thenReturnBadRequest() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(400, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        PaymentReportEmailData emailData = new PaymentReportEmailData(null, null, null, null, false);
        EmailDocument<PaymentReportEmailData> document = getPaymentEmailDocument(emailData);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(400));
    }

    @Test
    void givenValidPayload_whenEmailClientThrowsApiException_thenReturnError() throws ApiErrorResponseException {
        // Arrange:
        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenThrow(ApiErrorResponseException.class);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        PaymentReportEmailData emailData = new PaymentReportEmailData("unit@test.com", "My Payment Subject", "file://file-link", "filename.pdf", false);
        EmailDocument<PaymentReportEmailData> document = getPaymentEmailDocument(emailData);

        // Act:
        EmailClientException expectedException = assertThrows(EmailClientException.class, () ->
                emailClient.sendEmail(document)
        );

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(expectedException.getMessage(), is("Error sending payload to CHS Kafka API: "));
    }

    @Test
    void givenInvalidPayload_whenEmailClientThrowsJsonException_thenReturnError() throws JsonProcessingException {
        // Arrange:
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        ReflectionTestUtils.setField(emailClient, "objectMapper", mockObjectMapper);

        PaymentReportEmailData emailData = new PaymentReportEmailData("unit@test.com", "My Payment Subject", "file://file-link", "filename.pdf", false);
        EmailDocument<PaymentReportEmailData> document = getPaymentEmailDocument(emailData);

        // Act:
        EmailClientException expectedException = assertThrows(EmailClientException.class, () ->
                emailClient.sendEmail(document)
        );

        // Assert:
        verifyNoInteractions(internalApiClient);

        assertThat(expectedException.getMessage(), is("Error creating payload for CHS Kafka API: "));
    }

    private <T> EmailDocument<T> getPaymentEmailDocument(final T data) {
        return EmailDocument.<T>builder()
                .withTopic("test-email-topic")
                .withMessageId(UUID.randomUUID().toString())
                .withRecipientEmailAddress("unit-test@ch.gov.uk")
                .withEmailTemplateAppId("email-template-app-id")
                .withEmailTemplateMessageType("email-template-message-type")
                .withData(data)
                .withCreatedAt(LocalDateTime.now().toString())
                .build();
    }
}
