package uk.gov.companieshouse.efs.api.email;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;
import org.apache.avro.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSH19SameDaySubmissionSupportEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionBusinessEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionSupportEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalAcceptEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalNotificationEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalRejectEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalAvFailedEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalFailedConversionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalSubmissionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.PaymentReportEmailMapper;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailData;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private EmailServiceImpl emailService;

    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Mock
    private CHKafkaProducer producer;

    @Mock
    private EmailSerialiser serializer;

    @Mock
    private Schema schema;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

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
    private EmailDocument<ExternalConfirmationEmailData> externalNotificationEmailDocument;

    @Mock
    private EmailDocument<ExternalAcceptEmailData> externalAcceptEmailDocument;

    @Mock
    private EmailDocument<ExternalRejectEmailData> externalRejectEmailDocument;

    @Mock
    private EmailDocument<InternalAvFailedEmailData> internalAVFailedEmailDocument;

    @Mock
    private EmailDocument<InternalFailedConversionEmailData> internalFailedConversionEmailDocument;

    @Mock
    private EmailDocument<InternalSubmissionEmailData> internalSubmissionEmailDocument;

    @Mock
    private EmailDocument<DelayedSubmissionSupportEmailData> delayedSubmissionSupportEmailDataEmailDocument;

    @Mock
    private EmailDocument<DelayedSubmissionBusinessEmailData> delayedSubmissionBusinessEmailDataEmailDocument;

    @Mock
    private EmailDocument<PaymentReportEmailData> paymentReportEmailDocument;
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
        this.emailService = new EmailServiceImpl(producer, serializer, schema, emailMapperFactory, timestampGenerator);
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionAccepted() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(externalAcceptEmailDocument.getTopic()).thenReturn("external-email-send");
        when(emailMapperFactory.getAcceptEmailMapper()).thenReturn(acceptEmailMapper);
        when(acceptEmailMapper.map(any())).thenReturn(externalAcceptEmailDocument);
        when(submission.getId()).thenReturn("abc");
        when(externalAcceptEmailModel.getSubmission()).thenReturn(submission);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendExternalAccept(externalAcceptEmailModel);

        // then
        verify(serializer).serialize(eq(externalAcceptEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("external-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionRejected() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(externalRejectEmailDocument.getTopic()).thenReturn("external-email-send");
        when(emailMapperFactory.getRejectEmailMapper()).thenReturn(rejectEmailMapper);
        when(rejectEmailMapper.map(any())).thenReturn(externalRejectEmailDocument);
        when(submission.getId()).thenReturn("abc");
        when(externalRejectEmailModel.getSubmission()).thenReturn(submission);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendExternalReject(externalRejectEmailModel);

        // then
        verify(serializer).serialize(eq(externalRejectEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("external-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionHasInfectedFiles() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(internalAVFailedEmailDocument.getTopic()).thenReturn("internal-email-send");
        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(any())).thenReturn(internalAVFailedEmailDocument);
        when(internalAVFailedEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendInternalFailedAV(internalAVFailedEmailModel);

        // then
        verify(serializer).serialize(eq(internalAVFailedEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("internal-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenFilesFailConversion() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(internalFailedConversionEmailDocument.getTopic()).thenReturn("internal-email-send");
        when(emailMapperFactory.getInternalFailedConversionEmailMapper()).thenReturn(internalFailedConversionEmailMapper);
        when(internalFailedConversionEmailMapper.map(any())).thenReturn(internalFailedConversionEmailDocument);
        when(internalFailedConversionModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendInternalFailedConversion(internalFailedConversionModel);

        // then
        verify(serializer).serialize(eq(internalFailedConversionEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("internal-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceThrowsEmailServiceExceptionWhenProducerThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        // given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(internalAVFailedEmailDocument.getTopic()).thenReturn("internal-email-send");
        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(any())).thenReturn(internalAVFailedEmailDocument);
        when(internalAVFailedEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());
        doThrow(ExecutionException.class).when(producer).send(any());

        // when
        Executable actual = () -> this.emailService.sendInternalFailedAV(internalAVFailedEmailModel);

        // then
        EmailServiceException ex = assertThrows(EmailServiceException.class, actual);
        assertEquals("Error sending message to kafka", ex.getMessage());

    }

    @Test
    void testEmailServiceThrowsEmailServiceExceptionWhenProducerThrowsInterruptedException()
            throws ExecutionException, InterruptedException {
        // given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(internalAVFailedEmailDocument.getTopic()).thenReturn("internal-email-send");
        when(emailMapperFactory.getInternalAvFailedEmailMapper()).thenReturn(internalAVFailedEmailMapper);
        when(internalAVFailedEmailMapper.map(any())).thenReturn(internalAVFailedEmailDocument);
        when(internalAVFailedEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());
        doThrow(InterruptedException.class).when(producer).send(any());

        // when
        Executable actual = () -> this.emailService.sendInternalFailedAV(internalAVFailedEmailModel);

        // then
        EmailServiceException ex = assertThrows(EmailServiceException.class, actual);
        assertEquals("Error - thread interrupted", ex.getMessage());
    }

    @Test
    void testSendExternalConfirmation() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(externalNotificationEmailDocument.getTopic()).thenReturn("confirm-email-send");
        when(externalNotificationEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");
        when(emailMapperFactory.getConfirmationEmailMapper()).thenReturn(notificationEmailMapper);
        when(notificationEmailMapper.map(any())).thenReturn(externalNotificationEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        //when
        this.emailService.sendExternalConfirmation(externalNotificationEmailModel);

        //then
        verify(notificationEmailMapper).map(externalNotificationEmailModel);
        verify(serializer).serialize(eq(externalNotificationEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("confirm-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testSendExternalPaymentFailedNotification() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(externalNotificationEmailDocument.getTopic()).thenReturn("notification-email-send");
        when(externalNotificationEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("123");
        when(emailMapperFactory.getPaymentFailedEmailMapper()).thenReturn(notificationEmailMapper);
        when(notificationEmailMapper.map(any())).thenReturn(externalNotificationEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        //when
        this.emailService.sendExternalPaymentFailedNotification(externalNotificationEmailModel);

        //then
        verify(notificationEmailMapper).map(externalNotificationEmailModel);
        verify(serializer).serialize(eq(externalNotificationEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("notification-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionNotFesEnabled() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(internalSubmissionEmailDocument.getTopic()).thenReturn("internal-email-send");
        when(emailMapperFactory.getInternalSubmissionEmailMapper()).thenReturn(internalSubmissionEmailMapper);
        when(internalSubmissionEmailMapper.map(any())).thenReturn(internalSubmissionEmailDocument);
        when(internalSubmissionEmailModel.getSubmission()).thenReturn(submission);
        when(submission.getId()).thenReturn("abc");
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendInternalSubmission(internalSubmissionEmailModel);

        // then
        verify(serializer).serialize(eq(internalSubmissionEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("internal-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }


    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionDelayed() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(delayedSubmissionSupportEmailDataEmailDocument.getTopic()).thenReturn("delayed-submission-support-email-send");
        when(emailMapperFactory.getDelayedSubmissionSupportEmailMapper()).thenReturn(delayedSubmissionSupportEmailMapper);
        when(delayedSubmissionSupportEmailMapper.map(any())).thenReturn(delayedSubmissionSupportEmailDataEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendDelayedSubmissionSupportEmail(delayedSubmissionSupportEmailModel);

        // then
        verify(serializer).serialize(eq(delayedSubmissionSupportEmailDataEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("delayed-submission-support-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSH19SameDaySubmissionDelayed() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(delayedSubmissionSupportEmailDataEmailDocument.getTopic()).thenReturn(
            "delayed-submission-support-email-send");
        when(emailMapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper()).thenReturn(
            delayedSH19SameDaySubmissionSupportEmailMapper);
        when(delayedSH19SameDaySubmissionSupportEmailMapper.map(any())).thenReturn(delayedSubmissionSupportEmailDataEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendDelayedSH19SubmissionSupportEmail(delayedSubmissionSupportEmailModel);

        // then
        verify(serializer).serialize(eq(delayedSubmissionSupportEmailDataEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("delayed-submission-support-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenSubmissionVeryDelayed() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(delayedSubmissionBusinessEmailDataEmailDocument.getTopic()).thenReturn("delayed-submission-business-email-send");
        when(emailMapperFactory.getDelayedSubmissionBusinessEmailMapper()).thenReturn(delayedSubmissionBusinessEmailMapper);
        when(delayedSubmissionBusinessEmailMapper.map(any())).thenReturn(delayedSubmissionBusinessEmailDataEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendDelayedSubmissionBusinessEmail(delayedSubmissionBusinessEmailModel);

        // then
        verify(serializer).serialize(eq(delayedSubmissionBusinessEmailDataEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("delayed-submission-business-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

    @Test
    void testEmailServiceSendsMessageToKafkaWhenPaymentReportRequested() throws ExecutionException, InterruptedException {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(paymentReportEmailDocument.getTopic()).thenReturn("external-email-send");
        when(emailMapperFactory.getPaymentReportEmailMapper()).thenReturn(paymentReportEmailMapper);
        when(paymentReportEmailMapper.map(any())).thenReturn(paymentReportEmailDocument);
        when(serializer.serialize(any(), any())).thenReturn("Hello".getBytes());

        // when
        this.emailService.sendPaymentReportEmail(paymentReportEmailModel);

        // then
        verify(serializer).serialize(eq(paymentReportEmailDocument), eq(schema));
        verify(producer).send(messageCaptor.capture());
        assertEquals("external-email-send", messageCaptor.getValue().getTopic());
        assertEquals(createAtLocalDateTime.toEpochSecond(ZoneOffset.UTC), messageCaptor.getValue().getTimestamp());
        assertArrayEquals("Hello".getBytes(), messageCaptor.getValue().getValue());
    }

}
