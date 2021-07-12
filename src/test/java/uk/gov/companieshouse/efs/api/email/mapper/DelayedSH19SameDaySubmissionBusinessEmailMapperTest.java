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
import uk.gov.companieshouse.efs.api.email.config.DelayedSH19SameDaySubmissionBusinessEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class DelayedSH19SameDaySubmissionBusinessEmailMapperTest {
    private DelayedSH19SameDaySubmissionBusinessEmailMapper testMapper;

    @Mock
    private DelayedSH19SameDaySubmissionBusinessEmailConfig config;
    @Mock
    private IdentifierGeneratable idGenerator;
    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    @Mock
    private DelayedSubmissionBusinessEmailModel model;
    @Mock
    private DelayedSubmissionBusinessModel delayedSubmissionBusinessModel;

    @BeforeEach
    void setUp() {
        testMapper = new DelayedSH19SameDaySubmissionBusinessEmailMapper(config, idGenerator,
            timestampGenerator);
    }

    @Test
    void mapBusinessModelToEmailDocument() {
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS submission delay alert: Same day SH19");
        when(config.getAppId()).thenReturn(
            "efs-submission-api.efs_submission_delayed_sh19_sameday");
        when(config.getMessageType()).thenReturn("efs_submission_delayed_sh19_sameday");
        when(config.getTopic()).thenReturn("email-send");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");
        when(model.getDelayedSubmissions()).thenReturn(
            Collections.singletonList(delayedSubmissionBusinessModel));
        when(model.getEmailAddress()).thenReturn("sh19@ch.gov.uk");
        when(model.getDelayInMinutes()).thenReturn(60);

        //when
        EmailDocument<DelayedSubmissionBusinessEmailData> actual = testMapper.map(model);

        //then
        assertEquals(expectedDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<DelayedSubmissionBusinessEmailData> expectedDocument() {
        return EmailDocument.<DelayedSubmissionBusinessEmailData>builder()
            .withEmailTemplateAppId("efs-submission-api.efs_submission_delayed_sh19_sameday")
            .withMessageId("123")
            .withEmailTemplateMessageType("efs_submission_delayed_sh19_sameday")
            .withRecipientEmailAddress("sh19@ch.gov.uk")
            .withCreatedAt("02 June 2020")
            .withTopic("email-send")
            .withData(
                DelayedSubmissionBusinessEmailData.builder()
                    .withTo("sh19@ch.gov.uk")
                    .withSubject("EFS submission delay alert: Same day SH19")
                    .withDelayedSubmissions(Collections.singletonList(delayedSubmissionBusinessModel))
                    .withThresholdInMinutes(60)
                    .build())
            .build();
    }

}