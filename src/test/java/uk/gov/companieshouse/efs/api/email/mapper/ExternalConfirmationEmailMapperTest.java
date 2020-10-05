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

import uk.gov.companieshouse.efs.api.email.config.ExternalConfirmationEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
public class ExternalConfirmationEmailMapperTest {

    private ExternalConfirmationEmailMapper confirmationEmailMapper;

    @Mock
    private ExternalConfirmationEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Mock
    private Submission submission;

    @Mock
    private ExternalConfirmationEmailModel externalConfirmationEmailModel;

    @Mock
    private Company company;

    @Mock
    private FormDetails formDetails;

    @Mock
    private FileDetails fileDetails;

    @Mock
    private Presenter presenter;

    @BeforeEach
    void setUp() {
        this.confirmationEmailMapper = new ExternalConfirmationEmailMapper(config, idGenerator, timestampGenerator);
    }

    @Test
    void mapSubmissionDataToConfirmationEmailModel() {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(config.getSubject()).thenReturn("Your document has been submitted");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_confirmation");
        when(config.getMessageType()).thenReturn("efs_submission_confirmation");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        when(idGenerator.generateId()).thenReturn("123");

        when(submission.getCompany()).thenReturn(company);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("SH01");
        when(presenter.getEmail()).thenReturn("demo@ch.gov.uk");
        when(submission.getPresenter()).thenReturn(presenter);

        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");

        when(formDetails.getFileDetailsList()).thenReturn(Collections.singletonList(fileDetails));

        when(externalConfirmationEmailModel.getSubmission()).thenReturn(submission);

        //when
        EmailDocument<ExternalConfirmationEmailData> actual = confirmationEmailMapper.map(externalConfirmationEmailModel);

        //then
        assertEquals(expectedConfirmationEmailDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
    }

    private EmailDocument<ExternalConfirmationEmailData> expectedConfirmationEmailDocument() {
        return EmailDocument.<ExternalConfirmationEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_confirmation")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_confirmation")
                .withRecipientEmailAddress("demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        ExternalConfirmationEmailData.builder()
                                .withTo("demo@ch.gov.uk")
                                .withSubject("Your document has been submitted")
                                .withCompany(company)
                                .withFormType("SH01")
                                .withConfirmationReference("abcd3434343efsfg")
                                .withEmailFileDetailsList(Collections.singletonList(new EmailFileDetails(fileDetails, null)))
                                .withPresenter(presenter)
                                .build())
                .build();
    }

}
