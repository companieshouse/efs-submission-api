package uk.gov.companieshouse.efs.api.email.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.DelayedSubmissionBusinessEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailData;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessModel;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionBusinessEmailMapperTest {

    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;

    @Mock
    private DelayedSubmissionBusinessEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Mock
    private DelayedSubmissionBusinessEmailModel delayedSubmissionBusinessEmailModel;

    @Mock
    private DelayedSubmissionBusinessModel delayedSubmissionBusinessModel;

    @BeforeEach
    void setUp() {
        this.delayedSubmissionBusinessEmailMapper = new DelayedSubmissionBusinessEmailMapper(config, idGenerator, timestampGenerator);
    }

    @Test
    void testMapBusinessModelToEmailDocument() {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS Submission delayed submission");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_delayed_submission_business");
        when(config.getMessageType()).thenReturn("efs_submission_delayed_submission_business");
        when(config.getTopic()).thenReturn("email-send");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");
        when(delayedSubmissionBusinessEmailModel.getDelayedSubmissions()).thenReturn(Collections.singletonList(delayedSubmissionBusinessModel));
        when(delayedSubmissionBusinessEmailModel.getEmailAddress()).thenReturn("internal_RP_demo@ch.gov.uk");
        when(delayedSubmissionBusinessEmailModel.getDelayInMinutes()).thenReturn(72*60);

        //when
        EmailDocument<DelayedSubmissionBusinessEmailData> actual = delayedSubmissionBusinessEmailMapper.map(delayedSubmissionBusinessEmailModel);

        //then
        assertEquals(expectedDelayedSubmissionBusinessDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<DelayedSubmissionBusinessEmailData> expectedDelayedSubmissionBusinessDocument() {
        return EmailDocument.<DelayedSubmissionBusinessEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_delayed_submission_business")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_delayed_submission_business")
                .withRecipientEmailAddress("internal_RP_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        DelayedSubmissionBusinessEmailData.builder()
                                .withTo("internal_RP_demo@ch.gov.uk")
                                .withSubject("EFS Submission delayed submission")
                                .withDelayedSubmissions(Collections.singletonList(delayedSubmissionBusinessModel))
                                .withThresholdInMinutes(72*60)
                                .build())
                .build();
    }
}