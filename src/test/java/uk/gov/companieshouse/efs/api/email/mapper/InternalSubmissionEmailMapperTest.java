package uk.gov.companieshouse.efs.api.email.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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

import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.email.FormCategoryToEmailAddressService;
import uk.gov.companieshouse.efs.api.email.config.InternalSubmissionEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailData;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@ExtendWith(MockitoExtension.class)
class InternalSubmissionEmailMapperTest {

    private InternalSubmissionEmailMapper mapper;

    @Mock
    private InternalSubmissionEmailConfig config;

    @Mock
    private IdentifierGeneratable idGenerator;

    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    @Mock
    private InternalSubmissionEmailModel model;

    @Mock
    private Company company;

    @Mock
    private FormDetails formDetails;

    @Mock
    private FileDetails fileDetails;

    @Mock
    private Presenter presenter;

    @Mock
    private Submission submission;

    @Mock
    private EmailFileDetails emailFileDetails;

    @Mock
    private FormCategoryToEmailAddressService emailAddressService;

    @Mock
    private CategoryTemplateService categoryTemplateService;

    @Mock
    private FormTemplateService formTemplateService;

    @Mock
    private FormTemplateApi formTemplateApi;

    @BeforeEach
    void setUp() {
        this.mapper = new InternalSubmissionEmailMapper(config, idGenerator, timestampGenerator, emailAddressService,
                categoryTemplateService, formTemplateService);
    }

    @Test
    void mapSubmissionDataToConfirmationEmailModel() {
        // given
        final LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("Your document has been submitted");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_confirmation");
        when(config.getMessageType()).thenReturn("efs_submission_confirmation");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(model.emailFileDetailsList()).thenReturn(Collections.singletonList(emailFileDetails));
        when(emailFileDetails.getFileLink()).thenReturn("/file/link");
        when(emailFileDetails.getFileDetails()).thenReturn(fileDetails);
        when(model.submission()).thenReturn(submission);
        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");
        when(submission.getCompany()).thenReturn(company);
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("SH01");

        when(submission.getPresenter()).thenReturn(presenter);
        when(emailAddressService.getEmailAddressForFormCategory(anyString())).thenReturn("internal_demo@ch.gov.uk");
        when(formTemplateService.getFormTemplate("SH01")).thenReturn(formTemplateApi);
        when(formTemplateApi.getFormCategory()).thenReturn("SH");
        when(categoryTemplateService.getTopLevelCategory("SH")).thenReturn(CategoryTypeConstants.SHARE_CAPITAL);

        // when
        final EmailDocument<InternalSubmissionEmailData> actual = mapper.map(model);

        // then
        assertEquals(expectedInternalSubmissionEmailDocument(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
        verify(emailAddressService).getEmailAddressForFormCategory("SH01");
    }

    private EmailDocument<InternalSubmissionEmailData> expectedInternalSubmissionEmailDocument() {
        return EmailDocument.<InternalSubmissionEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_confirmation")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_confirmation")
                .withRecipientEmailAddress("internal_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(new InternalSubmissionEmailData(
                        "internal_demo@ch.gov.uk",
                        "Your document has been submitted",
                        "abcd3434343efsfg",
                        presenter,
                        company,
                        "SH01",
                        Collections.singletonList(new EmailFileDetails(fileDetails, "/file/link"))
                ))
                .build();
    }

    @Test
    void mapSubmissionDataToConfirmationEmailModelRegPowers() {
        // given
        final LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("Your document has been submitted");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_confirmation");
        when(config.getMessageType()).thenReturn("efs_submission_confirmation");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(model.emailFileDetailsList()).thenReturn(Collections.singletonList(emailFileDetails));
        when(emailFileDetails.getFileLink()).thenReturn("/file/link");
        when(emailFileDetails.getFileDetails()).thenReturn(fileDetails);
        when(model.submission()).thenReturn(submission);
        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn("12345678");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("RP02A");

        when(submission.getPresenter()).thenReturn(presenter);
        when(emailAddressService.getEmailAddressForRegPowersFormCategory(
                "RP02A", "12345678")).thenReturn("internal_RP_demo@ch.gov.uk");
        when(formTemplateService.getFormTemplate("RP02A")).thenReturn(formTemplateApi);
        when(formTemplateApi.getFormCategory()).thenReturn("RP");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.REGISTRAR_POWERS);

        // when
        final EmailDocument<InternalSubmissionEmailData> actual = mapper.map(model);

        // then
        assertEquals(expectedInternalSubmissionEmailDocumentRegPowers(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
        verify(emailAddressService).getEmailAddressForRegPowersFormCategory("RP02A", "12345678");
    }

    private EmailDocument<InternalSubmissionEmailData> expectedInternalSubmissionEmailDocumentRegPowers() {
        return EmailDocument.<InternalSubmissionEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_confirmation")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_confirmation")
                .withRecipientEmailAddress("internal_RP_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(new InternalSubmissionEmailData(
                        "internal_RP_demo@ch.gov.uk",
                        "Your document has been submitted",
                        "abcd3434343efsfg",
                        presenter,
                        company,
                        "RP02A",
                        Collections.singletonList(new EmailFileDetails(fileDetails, "/file/link"))
                ))
                .build();
    }

    @Test
    void mapSubmissionDataToConfirmationEmailModelRegPowersScot() {
        // given
        final LocalDateTime createAtLocalDateTime = LocalDateTime.of(2020, Month.JUNE, 2, 0, 0);

        when(config.getSubject()).thenReturn("Your document has been submitted");
        when(config.getAppId()).thenReturn("efs-submission-api.efs_submission_confirmation");
        when(config.getMessageType()).thenReturn("efs_submission_confirmation");
        when(config.getTopic()).thenReturn("email-send");
        when(config.getDateFormat()).thenReturn("dd MMMM yyyy");

        when(idGenerator.generateId()).thenReturn("123");
        when(timestampGenerator.generateTimestamp()).thenReturn(createAtLocalDateTime);

        when(model.emailFileDetailsList()).thenReturn(Collections.singletonList(emailFileDetails));
        when(emailFileDetails.getFileLink()).thenReturn("/file/link");
        when(emailFileDetails.getFileDetails()).thenReturn(fileDetails);
        when(model.submission()).thenReturn(submission);
        when(submission.getConfirmationReference()).thenReturn("abcd3434343efsfg");
        when(submission.getCompany()).thenReturn(company);
        when(company.getCompanyNumber()).thenReturn("SC12345678");
        when(submission.getFormDetails()).thenReturn(formDetails);
        when(formDetails.getFormType()).thenReturn("RP02A");

        when(submission.getPresenter()).thenReturn(presenter);
        when(emailAddressService.getEmailAddressForRegPowersFormCategory(
                "RP02A", "SC12345678")).thenReturn("internal_RP_Scot_demo@ch.gov.uk");
        when(formTemplateService.getFormTemplate("RP02A")).thenReturn(formTemplateApi);
        when(formTemplateApi.getFormCategory()).thenReturn("RP");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.REGISTRAR_POWERS);

        // when
        final EmailDocument<InternalSubmissionEmailData> actual = mapper.map(model);

        // then
        assertEquals(expectedInternalSubmissionEmailDocumentRegPowersScot(), actual);
        verify(idGenerator).generateId();
        verify(timestampGenerator).generateTimestamp();
        verify(emailAddressService).getEmailAddressForRegPowersFormCategory("RP02A", "SC12345678");
    }

    private EmailDocument<InternalSubmissionEmailData> expectedInternalSubmissionEmailDocumentRegPowersScot() {
        return EmailDocument.<InternalSubmissionEmailData>builder()
                .withEmailTemplateAppId("efs-submission-api.efs_submission_confirmation")
                .withMessageId("123")
                .withEmailTemplateMessageType("efs_submission_confirmation")
                .withRecipientEmailAddress("internal_RP_Scot_demo@ch.gov.uk")
                .withCreatedAt("02 June 2020")
                .withTopic("email-send")
                .withData(new InternalSubmissionEmailData(
                        "internal_RP_Scot_demo@ch.gov.uk",
                        "Your document has been submitted",
                        "abcd3434343efsfg",
                        presenter,
                        company,
                        "RP02A",
                        Collections.singletonList(new EmailFileDetails(fileDetails, "/file/link"))
                ))
                .build();
    }
}
