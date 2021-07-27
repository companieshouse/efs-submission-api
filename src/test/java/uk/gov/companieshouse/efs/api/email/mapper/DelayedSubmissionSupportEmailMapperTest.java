package uk.gov.companieshouse.efs.api.email.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.config.DelayedSubmissionSupportEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionSupportEmailMapperTest {

    private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;

    @Mock
    private DelayedSubmissionSupportEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<Instant> timestampGenerator;

    @Mock
    private Submission submission;

    @Mock
    private DelayedSubmissionSupportEmailModel delayedSubmissionSupportEmailModel;

    @Mock
    private DelayedSubmissionSupportModel delayedSubmissionSupportModel;

    @BeforeEach
    void setUp() {
        this.delayedSubmissionSupportEmailMapper = new DelayedSubmissionSupportEmailMapper(config, idGenerator,
                timestampGenerator);
    }

    @Test
    void mapSubmissionDataToDelayedSupportEmailModel() {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS Submission delayed submission");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_delayed_submission_support");
        when(config.getMessageType()).thenReturn("efs_submission_delayed_submission_support");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getSupportEmailAddress()).thenReturn("internal_RP_demo@ch.gov.uk");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime.atZone(ZoneId.of("UTC")).toInstant());

        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        when(delayedSubmissionSupportEmailModel.getDelayedSubmissions()).thenReturn(Collections.singletonList(delayedSubmissionSupportModel));
        when(delayedSubmissionSupportEmailModel.getThresholdInMinutes()).thenReturn(99);

        //when
        EmailDocument<DelayedSubmissionSupportEmailData> actual = delayedSubmissionSupportEmailMapper.map(delayedSubmissionSupportEmailModel);

        //then
        assertEquals(expectedDelayedSubmissionSupportDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<DelayedSubmissionSupportEmailData> expectedDelayedSubmissionSupportDocument() {
        return EmailDocument.<DelayedSubmissionSupportEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_delayed_submission_support")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_delayed_submission_support")
                .withRecipientEmailAddress("internal_RP_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        DelayedSubmissionSupportEmailData.builder()
                                .withTo("internal_RP_demo@ch.gov.uk")
                                .withSubject("EFS Submission delayed submission")
                                .withDelayedSubmissions(Collections.singletonList(delayedSubmissionSupportModel))
                                .withThresholdInMinutes(99)
                                .build())
                .build();
    }


}
