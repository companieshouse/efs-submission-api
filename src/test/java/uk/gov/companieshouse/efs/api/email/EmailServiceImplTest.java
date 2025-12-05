package uk.gov.companieshouse.efs.api.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.efs.api.client.EmailClient;
import uk.gov.companieshouse.efs.api.client.exception.EmailClientException;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.mapper.*;
import uk.gov.companieshouse.efs.api.email.model.*;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private EmailServiceImpl emailService;

    @Mock
    private EmailClient emailClient;

    @Captor
    private ArgumentCaptor<EmailDocument<?>> emailDocumentCaptor;

    @Mock
    private Submission submission;

    @Mock
    private ExternalAcceptEmailModel externalAcceptEmailModel;

    @Mock
    private ExternalRejectEmailModel externalRejectEmailModel;

    @Mock
    private InternalAvFailedEmailModel internalAVFailedEmailModel;

    @Mock
    private InternalFailedConversionModel internalFailedConversionModel;

    @Mock
    private ExternalNotificationEmailModel externalNotificationEmailModel;

    @Mock
    private InternalSubmissionEmailModel internalSubmissionEmailModel;

    @Mock
    private DelayedSubmissionSupportEmailModel delayedSubmissionSupportEmailModel;

    @Mock
    private DelayedSubmissionBusinessEmailModel delayedSubmissionBusinessEmailModel;

    @Mock
    private PaymentReportEmailModel paymentReportEmailModel;

    @Mock
    private EmailDocument<InternalAvFailedEmailData> internalAVFailedEmailDocument;

    @Mock
    private EmailMapperFactory emailMapperFactory;

    @Mock
    private ExternalNotificationEmailMapper notificationEmailMapper;

    @Mock
    private ExternalAcceptEmailMapper acceptEmailMapper;

    @Mock
    private ExternalRejectEmailMapper rejectEmailMapper;

    @Mock
    private InternalAvFailedEmailMapper internalAVFailedEmailMapper;

    @Mock
    private InternalFailedConversionEmailMapper internalFailedConversionEmailMapper;

    @Mock
    private InternalSubmissionEmailMapper internalSubmissionEmailMapper;

    @Mock
    private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;
    
    @Mock
    private DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper;

    @Mock
    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;

    @Mock
    private PaymentReportEmailMapper paymentReportEmailMapper;

    @BeforeEach
    void setUp() {
        this.emailService = new EmailServiceImpl(emailMapperFactory, emailClient);
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionAccepted() throws EmailServiceException {
        //given
        ExternalAcceptEmailData emailData = new ExternalAcceptEmailData(
                "unit@test.gov.uk",
                "My Subject Line",
                "CN000123",
                "My Company Name",
                "CONREF-001",
                "Form-Type",
                LocalDateTime.now().toString());

        EmailDocument<ExternalAcceptEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getAcceptEmailMapper()).thenReturn(acceptEmailMapper);
        when(acceptEmailMapper.map(externalAcceptEmailModel)).thenReturn(emailDocument);
        when(submission.getId()).thenReturn("abc");
        when(externalAcceptEmailModel.getSubmission()).thenReturn(submission);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendExternalAccept(externalAcceptEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getAcceptEmailMapper();
        verify(acceptEmailMapper, times(1)).map(externalAcceptEmailModel);
        verify(submission, times(1)).getId();
        verify(externalAcceptEmailModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionRejected() throws EmailServiceException{
        //given
        ExternalRejectEmailData emailData = new ExternalRejectEmailData(
                "unit@test.gov.uk",
                "My Subject Line",
                "CN000123",
                "My Company Name",
                "CONREF-001",
                "Form-Type",
                LocalDateTime.now().toString(),
                List.of("Not fussed"),
                true
        );

        EmailDocument<ExternalRejectEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getRejectEmailMapper()).thenReturn(rejectEmailMapper);
        when(rejectEmailMapper.map(externalRejectEmailModel)).thenReturn(emailDocument);
        when(submission.getId()).thenReturn("abc");
        when(externalRejectEmailModel.getSubmission()).thenReturn(submission);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendExternalReject(externalRejectEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getRejectEmailMapper();
        verify(rejectEmailMapper, times(1)).map(externalRejectEmailModel);
        verify(submission, times(1)).getId();
        verify(externalRejectEmailModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionHasInfectedFiles() throws EmailServiceException {
        //given
        InternalAvFailedEmailData emailData = InternalAvFailedEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .withCompanyNumber("CN000123")
                .withCompanyName("My Company Name")
                .withConfirmationReference("CONREF-001")
                .withFormType("Form-Type")
                .withRejectionDate(LocalDateTime.now().toString())
                .withInfectedFiles(List.of("file-1", "file-2"))
                .build();

        EmailDocument<InternalAvFailedEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(internalAVFailedEmailModel)).thenReturn(emailDocument);
        when(internalAVFailedEmailModel.submission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendInternalFailedAV(internalAVFailedEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getInternalAvFailedEmailMapper();
        verify(internalAVFailedEmailMapper, times(1)).map(internalAVFailedEmailModel);
        verify(submission, times(1)).getId();
        verify(internalAVFailedEmailModel, times(1)).submission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenFilesFailConversion() throws EmailServiceException {
        //given
        InternalFailedConversionEmailData emailData = InternalFailedConversionEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .withCompanyNumber("CN000123")
                .withCompanyName("My Company Name")
                .withConfirmationReference("CONREF-001")
                .withFormType("Form-Type")
                .withRejectionDate(LocalDateTime.now().toString())
                .withFailedToConvert(List.of("file-1", "file-2"))
                .build();

        EmailDocument<InternalFailedConversionEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getInternalFailedConversionEmailMapper()).thenReturn(internalFailedConversionEmailMapper);
        when(internalFailedConversionEmailMapper.map(internalFailedConversionModel)).thenReturn(emailDocument);
        when(internalFailedConversionModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendInternalFailedConversion(internalFailedConversionModel);

        // then
        verify(emailMapperFactory, times(1)).getInternalFailedConversionEmailMapper();
        verify(internalFailedConversionEmailMapper, times(1)).map(internalFailedConversionModel);
        verify(submission, times(1)).getId();
        verify(internalFailedConversionModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceThrowsEmailServiceExceptionWhenClientThrowsServiceException() throws EmailServiceException {
        // given
        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(any())).thenReturn(internalAVFailedEmailDocument);
        when(internalAVFailedEmailModel.submission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");

        doThrow(EmailClientException.class).when(emailClient).sendEmail(any());

        // when
        EmailServiceException expected = assertThrows(EmailServiceException.class, () ->
                emailService.sendInternalFailedAV(internalAVFailedEmailModel)
        );

        // then
        assertThat(expected.getMessage(), is("Error sending document to email client: "));
    }

    @Test
    void testEmailServiceThrowsEmailServiceExceptionWhenClientThrowsClientException() throws EmailServiceException {
        // given
        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(any())).thenReturn(internalAVFailedEmailDocument);
        when(internalAVFailedEmailModel.submission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");

        ApiResponse<Void> apiResponse = new ApiResponse<>(400, Map.of());
        when(emailClient.sendEmail(any())).thenReturn(apiResponse);

        // when
        EmailServiceException expected = assertThrows(EmailServiceException.class, () ->
                emailService.sendInternalFailedAV(internalAVFailedEmailModel)
        );

        // then
        assertThat(expected.getMessage(), is("Error sending request to CHS Kafka API: "));
    }

    @Test
    void testSendExternalConfirmation() throws EmailServiceException {
        //given
        ExternalConfirmationEmailData emailData = ExternalConfirmationEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .withCompany(new Company("CN000123","My Company Name"))
                .withConfirmationReference("CONREF-001")
                .withFormType("Form-Type")
                .build();

        EmailDocument<ExternalConfirmationEmailData> emailDocument = createEmailDocument(emailData);

        when(externalNotificationEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");
        when(emailMapperFactory.getConfirmationEmailMapper()).thenReturn(notificationEmailMapper);
        when(notificationEmailMapper.map(externalNotificationEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        //when
        this.emailService.sendExternalConfirmation(externalNotificationEmailModel);

        //then
        verify(emailMapperFactory, times(1)).getConfirmationEmailMapper();
        verify(notificationEmailMapper, times(1)).map(externalNotificationEmailModel);
        verify(submission, times(1)).getId();
        verify(externalNotificationEmailModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testSendExternalPaymentFailedNotification() throws EmailServiceException {
        //given
        ExternalConfirmationEmailData emailData = ExternalConfirmationEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .withCompany(new Company("CN000123","My Company Name"))
                .withConfirmationReference("CONREF-001")
                .withFormType("Form-Type")
                .build();

        EmailDocument<ExternalConfirmationEmailData> emailDocument = createEmailDocument(emailData);

        when(externalNotificationEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");
        when(emailMapperFactory.getPaymentFailedEmailMapper()).thenReturn(notificationEmailMapper);
        when(notificationEmailMapper.map(externalNotificationEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        //when
        this.emailService.sendExternalPaymentFailedNotification(externalNotificationEmailModel);

        //then
        verify(emailMapperFactory, times(1)).getPaymentFailedEmailMapper();
        verify(notificationEmailMapper, times(1)).map(externalNotificationEmailModel);
        verify(submission, times(1)).getId();
        verify(externalNotificationEmailModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionNotFesEnabled() throws EmailServiceException {
        //given
        InternalSubmissionEmailData emailData = InternalSubmissionEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .withCompany(new Company("CN000123","My Company Name"))
                .withConfirmationReference("CONREF-001")
                .withFormType("Form-Type")
                .build();

        EmailDocument<InternalSubmissionEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getInternalSubmissionEmailMapper()).thenReturn(internalSubmissionEmailMapper);
        when(internalSubmissionEmailMapper.map(internalSubmissionEmailModel)).thenReturn(emailDocument);
        when(internalSubmissionEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendInternalSubmission(internalSubmissionEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getInternalSubmissionEmailMapper();
        verify(internalSubmissionEmailMapper, times(1)).map(internalSubmissionEmailModel);
        verify(submission, times(1)).getId();
        verify(internalSubmissionEmailModel, times(1)).getSubmission();
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionDelayed() throws EmailServiceException {
        //given
        DelayedSubmissionSupportEmailData emailData = DelayedSubmissionSupportEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .build();

        EmailDocument<DelayedSubmissionSupportEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getDelayedSubmissionSupportEmailMapper()).thenReturn(delayedSubmissionSupportEmailMapper);
        when(delayedSubmissionSupportEmailMapper.map(delayedSubmissionSupportEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendDelayedSubmissionSupportEmail(delayedSubmissionSupportEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getDelayedSubmissionSupportEmailMapper();
        verify(delayedSubmissionSupportEmailMapper, times(1)).map(delayedSubmissionSupportEmailModel);
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSH19SameDaySubmissionDelayed() throws EmailServiceException {
        //given
        DelayedSubmissionSupportEmailData emailData = DelayedSubmissionSupportEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .build();

        EmailDocument<DelayedSubmissionSupportEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper()).thenReturn(delayedSH19SameDaySubmissionSupportEmailMapper);
        when(delayedSH19SameDaySubmissionSupportEmailMapper.map(delayedSubmissionSupportEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(org.mockito.ArgumentMatchers.<EmailDocument<?>>any())).thenReturn(apiResponse);

        // when
        this.emailService.sendDelayedSH19SubmissionSupportEmail(delayedSubmissionSupportEmailModel, "businessEmail");

        // then
        verify(emailMapperFactory, times(1)).getDelayedSH19SameDaySubmissionSupportEmailMapper();
        verify(delayedSH19SameDaySubmissionSupportEmailMapper, times(1)).map(any());
        verify(emailClient, times(2)).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        for (int i = 0; i < 2; ++i) {
            EmailDocument<?> emailDocumentValue = emailDocumentCaptor.getAllValues().get(i);

            assertEquals("email-template-app-id", emailDocumentValue.getAppId());
            assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentValue.getMessageId());
            assertEquals("email-template-message-type", emailDocumentValue.getMessageType());
            assertThat(emailDocument.getData(), is(emailData));

            // Service sends 2 emails, but the 2nd one has the "supplied" email address.
            String expectedEmailAddress = (i == 0) ? "unit-test@ch.gov.uk" : "businessEmail";
            assertEquals(expectedEmailAddress, emailDocumentValue.getEmailAddress());

            assertEquals("test-email-topic", emailDocumentValue.getTopic());
            assertEquals("2025-04-01T10:06:43.596087", emailDocumentValue.getCreatedAt());
        }
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenSubmissionVeryDelayed() throws EmailServiceException {
        //given
        DelayedSubmissionBusinessEmailData emailData = DelayedSubmissionBusinessEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .build();

        EmailDocument<DelayedSubmissionBusinessEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getDelayedSubmissionBusinessEmailMapper()).thenReturn(delayedSubmissionBusinessEmailMapper);
        when(delayedSubmissionBusinessEmailMapper.map(delayedSubmissionBusinessEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendDelayedSubmissionBusinessEmail(delayedSubmissionBusinessEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getDelayedSubmissionBusinessEmailMapper();
        verify(delayedSubmissionBusinessEmailMapper, times(1)).map(delayedSubmissionBusinessEmailModel);
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaApiWhenPaymentReportRequested() throws EmailServiceException {
        //given
        PaymentReportEmailData emailData = PaymentReportEmailData.builder()
                .withTo("unit@test.gov.uk")
                .withSubject("My Subject Line")
                .build();

        EmailDocument<PaymentReportEmailData> emailDocument = createEmailDocument(emailData);

        when(emailMapperFactory.getPaymentReportEmailMapper()).thenReturn(paymentReportEmailMapper);
        when(paymentReportEmailMapper.map(paymentReportEmailModel)).thenReturn(emailDocument);

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        // when
        this.emailService.sendPaymentReportEmail(paymentReportEmailModel);

        // then
        verify(emailMapperFactory, times(1)).getPaymentReportEmailMapper();
        verify(paymentReportEmailMapper, times(1)).map(paymentReportEmailModel);
        verify(emailClient).sendEmail(emailDocumentCaptor.capture());
        verify(emailClient, times(1)).sendEmail(emailDocument);

        assertEquals("email-template-app-id", emailDocumentCaptor.getValue().getAppId());
        assertEquals("dfb90835-c7cc-4293-a407-b18d2723a92e", emailDocumentCaptor.getValue().getMessageId());
        assertEquals("email-template-message-type", emailDocumentCaptor.getValue().getMessageType());
        assertThat(emailDocument.getData(), is(emailData));
        assertEquals("unit-test@ch.gov.uk", emailDocumentCaptor.getValue().getEmailAddress());
        assertEquals("test-email-topic", emailDocumentCaptor.getValue().getTopic());
        assertEquals("2025-04-01T10:06:43.596087", emailDocumentCaptor.getValue().getCreatedAt());
    }

    private <T> EmailDocument<T> createEmailDocument(final T data) {
        return EmailDocument.<T>builder()
                .withTopic("test-email-topic")
                .withMessageId("dfb90835-c7cc-4293-a407-b18d2723a92e")
                .withRecipientEmailAddress("unit-test@ch.gov.uk")
                .withEmailTemplateAppId("email-template-app-id")
                .withEmailTemplateMessageType("email-template-message-type")
                .withData(data)
                .withCreatedAt("2025-04-01T10:06:43.596087")
                .build();
    }

}
