package uk.gov.companieshouse.efs.api.email.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.efs.api.email.config.ExternalRejectedEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class ExternalRejectEmailMapperTest {

    private ExternalRejectEmailMapper rejectEmailMapper;

    @Mock
    private ExternalRejectedEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<Instant> timestampGenerator;

    @Mock
    private Submission submission;

    @Mock
    private ExternalRejectEmailModel externalRejectEmailModel;

    @Mock
    private Company company;

    @Mock
    private FormDetails formDetails;

    @Mock
    private Presenter presenter;

    @BeforeEach
    void setUp() {
        this.rejectEmailMapper = new ExternalRejectEmailMapper(config, idGenerator, timestampGenerator);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mapSubmissionDataToRejectEmailModel(boolean hasFee) {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS Submission rejected");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_external_reject");
        when(config.getMessageType()).thenReturn("efs_submission_external_reject");
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
        when(submission.getLastModifiedAt()).thenReturn(localDateTime);

        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");

        when(externalRejectEmailModel.getRejectReasons()).thenReturn(Collections.singletonList("ReasonList"));
        when(externalRejectEmailModel.getSubmission()).thenReturn(submission);

        when(submission.getFeeOnSubmission()).thenReturn(hasFee ? "10" : null);

        //when
        EmailDocument<ExternalRejectEmailData> actual = rejectEmailMapper.map(externalRejectEmailModel);

        //then
        assertEquals(expectedRejectEmailDocument(hasFee), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<ExternalRejectEmailData> expectedRejectEmailDocument(final boolean hasFee) {
        return EmailDocument.<ExternalRejectEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_external_reject")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_external_reject")
                .withRecipientEmailAddress("demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        ExternalRejectEmailData.builder()
                                .withTo("demo@ch.gov.uk")
                                .withSubject("EFS Submission rejected")
                                .withCompanyNumber("12345678")
                                .withCompanyName("ABC Co Ltd")
                                .withFormType("SH01")
                                .withConfirmationReference("abcd3434343efsfg")
                                .withRejectionDate("02 May 2020")
                                .withRejectReasons(Collections.singletonList("ReasonList"))
                                .withIsPaidForm(hasFee)
                                .build())
                .build();
    }

}
