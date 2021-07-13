package uk.gov.companieshouse.efs.api.email.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.config.DelayedSH19SameDaySubmissionSupportEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class DelayedSH19SameDaySubmissionSupportEmailMapperTest {
    private DelayedSH19SameDaySubmissionSupportEmailMapper testMapper;

    @Mock
    private DelayedSH19SameDaySubmissionSupportEmailConfig config;
    @Mock
    private IdentifierGeneratable idGenerator;
    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    @Mock
    private DelayedSubmissionSupportEmailModel model;
    @Mock
    private DelayedSubmissionSupportModel delayedSubmissionSupportModel;

    @BeforeEach
    void setUp() {
        testMapper = new DelayedSH19SameDaySubmissionSupportEmailMapper(config, idGenerator,
            timestampGenerator);
    }

    @Test
    void mapSupportModelToEmailDocument() {
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS submission delay alert: Same day SH19");
        when(config.getAppId()).thenReturn(
            "efs-submission-api.efs_submission_delayed_sh19_sameday");
        when(config.getMessageType()).thenReturn("efs_submission_delayed_sh19_sameday");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getSupportEmailAddress()).thenReturn("test_support@ch.gov.uk");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");
        when(model.getDelayedSubmissions()).thenReturn(
            Collections.singletonList(delayedSubmissionSupportModel));
        when(model.getThresholdInMinutes()).thenReturn(60);

        //when
        EmailDocument<DelayedSubmissionSupportEmailData> actual = testMapper.map(model);

        //then
        assertEquals(expectedDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<DelayedSubmissionSupportEmailData> expectedDocument() {
        return EmailDocument.<DelayedSubmissionSupportEmailData>builder()
            .withEmailTemplateAppId("efs-submission-api.efs_submission_delayed_sh19_sameday")
            .withMessageId("123")
            .withEmailTemplateMessageType("efs_submission_delayed_sh19_sameday")
            .withRecipientEmailAddress("test_support@ch.gov.uk")
            .withCreatedAt("02 June 2020")
            .withTopic("email-send")
            .withData(DelayedSubmissionSupportEmailData.builder()
                .withTo("test_support@ch.gov.uk")
                .withSubject("EFS submission delay alert: Same day SH19")
                .withDelayedSubmissions(Collections.singletonList(delayedSubmissionSupportModel))
                .withThresholdInMinutes(60)
                .build())
            .build();
    }
}