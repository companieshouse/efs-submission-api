package uk.gov.companieshouse.efs.api.email.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.config.ExternalAcceptedEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class ExternalAcceptEmailMapperTest {
    private ExternalAcceptEmailMapper acceptEmailMapper;

    @Mock
    private ExternalAcceptedEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<Instant> timestampGenerator;

    @Mock
    private Submission submission;

    @Mock
    private ExternalAcceptEmailModel externalAcceptEmailModel;

    @Mock
    private Company company;

    @Mock
    private FormDetails formDetails;

    @Mock
    private Presenter presenter;

    @BeforeEach
    void setUp() {
        this.acceptEmailMapper = new ExternalAcceptEmailMapper(config, idGenerator, timestampGenerator);
    }

    @Test
    void mapSubmissionDataToAcceptEmailModel() {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS Submission accepted");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_external_accept");
        when(config.getMessageType()).thenReturn("efs_submission_external_accept");
        when(config.getTopic()).thenReturn("email-send");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime.toInstant(
            ZoneOffset.UTC));

        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn("12345678");
        when(company.getCompanyName()).thenReturn("ABC Co Ltd");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("SH01");
        when(presenter.getEmail()).thenReturn("demo@ch.gov.uk");
        when(submission.getPresenter()).thenReturn(presenter);

        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        LocalDateTime localDateTime = LocalDateTime.of(2020, Month.MAY, 2, 0, 0);
        when(submission.getSubmittedAt()).thenReturn(localDateTime);

        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");

        when(externalAcceptEmailModel.getSubmission()).thenReturn(submission);

        //when
        EmailDocument<ExternalAcceptEmailData> actual = acceptEmailMapper.map(externalAcceptEmailModel);

        //then
        assertEquals(expectedAcceptEmailDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<ExternalAcceptEmailData> expectedAcceptEmailDocument() {
        return EmailDocument.<ExternalAcceptEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_external_accept").withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_external_accept")
                .withRecipientEmailAddress("demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        ExternalAcceptEmailData.builder()
                                .withTo("demo@ch.gov.uk")
                                .withSubject("EFS Submission accepted")
                                .withCompanyNumber("12345678")
                                .withCompanyName("ABC Co Ltd")
                                .withFormType("SH01")
                                .withConfirmationReference("abcd3434343efsfg")
                                .withSubmittedDate("02 May 2020")
                                .build())
                .build();
    }

}
