package uk.gov.companieshouse.efs.api.email.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.config.PaymentReportEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailData;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class PaymentReportEmailMapperTest {
    private PaymentReportEmailMapper testMapper;

    @Mock
    private PaymentReportEmailConfig config;
    @Mock
    private IdentifierGeneratable idGenerator;
    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    @Mock
    private PaymentReportEmailModel model;

    @BeforeEach
    void setUp() {
        testMapper = new PaymentReportEmailMapper(config, idGenerator, timestampGenerator);
    }

    @Test
    void mapFinanceEmail() {
        // given
        expectEmailContent(false);

        // when
        EmailDocument<PaymentReportEmailData> actual = testMapper.map(model);

        // then
        assertThat(actual, is(equalTo(expectedPaymentReportEmailDocument(false))));
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    @Test
    void mapScottishEmail() {
        // given
        expectEmailContent(true);

        // when
        EmailDocument<PaymentReportEmailData> actual = testMapper.map(model);

        // then
        assertThat(actual, is(equalTo(expectedPaymentReportEmailDocument(true))));
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private void expectEmailContent(final boolean isScottish) {
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 2, 2);

        when(config.getTopic()).thenReturn("email-send");
        when(config.getAppId()).thenReturn("efs-submission-api.payment_report");
        when(config.getMessageType()).thenReturn("efs_payment_report");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");
        if (isScottish) {
            when(config.getScottishEmailAddress()).thenReturn("scot_internal_demo@ch.gov.uk");
        }
        when(config.getFinanceEmailAddress()).thenReturn("internal_demo@ch.gov.uk");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(model.getFileLink()).thenReturn("file-link");
        when(model.getFileName()).thenReturn("file-name" + (isScottish ? "Scottish" : ""));
    }

    private EmailDocument<PaymentReportEmailData> expectedPaymentReportEmailDocument(final boolean isScottish) {
        final String fileName = "file-name" + (isScottish ? "Scottish" : "");
        return EmailDocument.<PaymentReportEmailData>builder()
            .withEmailTemplateAppId("efs-submission-api.payment_report").withMessageId("123")
            .withEmailTemplateMessageType("efs_payment_report").withRecipientEmailAddress("internal_demo@ch.gov.uk")
            .withCreatedAt("02 June 2020").withTopic("email-send").withData(
                PaymentReportEmailData.builder().withTo((isScottish ? "scot_" : "") + "internal_demo@ch.gov.uk")
                    .withSubject(fileName).withFileLink("file-link").withFileName(fileName).build()).build();
    }

}