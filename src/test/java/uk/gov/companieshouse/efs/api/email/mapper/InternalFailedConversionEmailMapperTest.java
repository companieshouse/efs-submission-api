package uk.gov.companieshouse.efs.api.email.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.InternalFailedConversionEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalFailedConversionEmailMapperTest {

    private InternalFailedConversionEmailMapper mapper;

    @Mock
    private InternalFailedConversionEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Mock
    private Submission submission;

    @Mock
    private InternalFailedConversionModel internalFailedConversionModel;

    @Mock
    private Company company;

    @Mock
    private FormDetails formDetails;

    @Mock
    private Presenter presenter;

    @Mock
    private FormCategoryToEmailAddressService emailAddressService;

    @BeforeEach
    void setUp() {
        this.mapper = new InternalFailedConversionEmailMapper(config, idGenerator, timestampGenerator, emailAddressService);
    }

    @Test
    void mapSubmissionDataToAcceptEmailModel() {
        //given
        LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("EFS Submission Failed Conversion");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_internal_failed_conversion");
        when(config.getMessageType()).thenReturn("efs_submission_internal_failed_conversion");
        when(config.getTopic()).thenReturn("email-send");
        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

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
        when(emailAddressService.getEmailAddressForFormCategory(anyString())).thenReturn("internal_RP_demo@ch.gov.uk");

        when(internalFailedConversionModel.getSubmission()).thenReturn(submission);
        when(internalFailedConversionModel.getFailedToConvert()).thenReturn(Collections.singletonList("failed.pdf"));

        //when
        EmailDocument<InternalFailedConversionEmailData> actual = mapper.map(internalFailedConversionModel);

        //then
        assertEquals(expectedFailedConversionDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
        verify(emailAddressService).getEmailAddressForFormCategory("SH01");
    }

    private EmailDocument<InternalFailedConversionEmailData> expectedFailedConversionDocument() {
        return EmailDocument.<InternalFailedConversionEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_internal_failed_conversion").withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_internal_failed_conversion")
                .withRecipientEmailAddress("internal_RP_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(
                        InternalFailedConversionEmailData.builder()
                                .withTo("internal_RP_demo@ch.gov.uk")
                                .withSubject("EFS Submission Failed Conversion")
                                .withCompanyNumber("12345678")
                                .withCompanyName("ABC Co Ltd")
                                .withFormType("SH01")
                                .withConfirmationReference("abcd3434343efsfg")
                                .withRejectionDate("02 May 2020")
                                .withFailedToConvert(Collections.singletonList("failed.pdf"))
                                .withUserEmail("demo@ch.gov.uk")
                                .build())
                .build();
    }

}
