package uk.gov.companieshouse.efs.api.email.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest(name = "Report type: {0}")
    @ValueSource(strings = {"", "scottish", "specCap"})
    void mapPaymentEmail(String reportType) {
        // given
        expectEmailContent(reportType);

        // when
        EmailDocument<PaymentReportEmailData> actual = testMapper.map(model);

        // then
        assertThat(actual, is(equalTo(expectedPaymentReportEmailDocument(reportType))));
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private void expectEmailContent(final String reportType) {
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 2, 2);

        when(config.getTopic()).thenReturn("email-send");
        when(config.getAppId()).thenReturn("efs-submission-api.payment_report");
        when(config.getMessageType()).thenReturn("efs_payment_report");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");
        if (StringUtils.equals("scottish", reportType)) {
            when(config.getScottishEmailAddress()).thenReturn("scot_internal_demo@ch.gov.uk");
        } else if (StringUtils.equals("specCap", reportType)) {
            when(config.getSpecialCapitalEmailAddress()).thenReturn("specCap_internal_demo@ch.gov.uk");
        } else {
            when(config.getFinanceEmailAddress()).thenReturn("internal_demo@ch.gov.uk");
        }
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(model.getFileLink()).thenReturn("file-link");
        if (StringUtils.equals("scottish", reportType)) {
            when(model.getFileName()).thenReturn("file-nameScottish");
        } else if (StringUtils.equals("specCap", reportType)) {
            when(model.getFileName()).thenReturn("file-nameSH19");
        } else {
            when(model.getFileName()).thenReturn("file-name");
        }
    }

    private EmailDocument<PaymentReportEmailData> expectedPaymentReportEmailDocument(final String reportType) {
        final String fileName;
        final String recipientEmailAddress;
        if (StringUtils.equals("scottish", reportType)) {
            fileName = "file-nameScottish";
            recipientEmailAddress = "scot_internal_demo@ch.gov.uk";
        } else if (StringUtils.equals("specCap", reportType)) {
            fileName = "file-nameSH19";
            recipientEmailAddress = "specCap_internal_demo@ch.gov.uk";
        } else {
            fileName = "file-name";
            recipientEmailAddress = "internal_demo@ch.gov.uk";
        }

        return EmailDocument.<PaymentReportEmailData>builder()
            .withEmailTemplateAppId("efs-submission-api.payment_report").withMessageId("123")
            .withEmailTemplateMessageType("efs_payment_report").withRecipientEmailAddress(recipientEmailAddress)
            .withCreatedAt("02 June 2020").withTopic("email-send").withData(
                PaymentReportEmailData.builder().withTo(getReportEmailAddress(reportType))
                    .withSubject(fileName).withFileLink("file-link").withFileName(fileName).build()).build();
    }

    private String getReportEmailAddress(final String reportType) {
        if (StringUtils.equals("scottish", reportType)) {
            return ("scot_internal_demo@ch.gov.uk");
        } else if (StringUtils.equals("specCap", reportType)) {
            return ("specCap_internal_demo@ch.gov.uk");
        } else {
            return ("internal_demo@ch.gov.uk");
        }
    }

}