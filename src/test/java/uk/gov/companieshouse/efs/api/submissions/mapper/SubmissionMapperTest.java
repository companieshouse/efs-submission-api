package uk.gov.companieshouse.efs.api.submissions.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Collections;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.RejectReasonApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

@ExtendWith(MockitoExtension.class)
class SubmissionMapperTest {

    private static final String SUBMISSION_ID = "1";
    private static final String CONFIRMATION_REFERENCE = "1 2 3 4";
    private static final String EMAIL_ADDRESS = "demo@ch.gov.uk";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "ACME";
    private static final SessionApi PAYMENT_SESSION = new SessionApi("7777777777", "random-state",
        PaymentTemplate.Status.PENDING.toString());
    private static final String FEE_ON_SUBMISSION = "17";
    private static final Boolean CONFIRM_AUTHORISED = true;
    private static final String BARCODE = "Y9999999";
    private static final String FORM_TYPE = "CC01";
    private static final String FILE_ID = "abc123";
    private static final String FILENAME = "HELLO.pdf";
    private static final long FILE_SIZE = 100L;
    private static final String CONVERTED_FILE_ID = "abc124";
    private static final String REJECT_REASON = "Test Reject Reason";
    private SubmissionMapper mapper;
    public static final SessionListApi PAYMENT_SESSIONS = new SessionListApi(Collections.singletonList(PAYMENT_SESSION));

    @BeforeEach
    void setUp() {
        this.mapper = new SubmissionMapper();
    }

    @Test
    void testMapSubmissionWithNullFormNullCompanyNullPresenter() {
        //when
        SubmissionApi actual = mapper.map(Submission.builder().build());

        //then
        assertEquals(new SubmissionApi(), actual);
    }

    @Test
    void testMapSubmissionWithNullFileDetailsNullCompanyNullPresenter() {
        //when
        SubmissionApi actual = mapper.map(Submission.builder().withFormDetails(FormDetails.builder().build()).build());

        //then
        assertEquals(
            new SubmissionApi(null, null, null, null, null, null, null, null, new SubmissionFormApi(), null, null, null,
                null), actual);
    }

    @Test
    void testMapSubmissionWithAllFieldsPresent() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Submission submission = submissionDataEntity(now);

        //when
        SubmissionApi actual = mapper.map(submission);

        //then
        assertEquals(expectedSubmissionApi(now), actual);
    }

    @Test
    void testSubmissionWithRejectReasons(){
        //given
        LocalDateTime now = LocalDateTime.now();
        Submission submission = submissionDataEntity(now);
        Submission updatedSubmission = Submission.builder(submission).withChipsRejectReasons(Collections.singletonList(new RejectReason(REJECT_REASON))).build();

        //when
        SubmissionApi actual = mapper.map(updatedSubmission);
        System.out.println("expect=" + ReflectionToStringBuilder.toString(expectedSubmissionApiWithRejectReasons(now), RecursiveToStringStyle.MULTI_LINE_STYLE));
        System.out.println("actual=" + ReflectionToStringBuilder.toString(actual, RecursiveToStringStyle.MULTI_LINE_STYLE));

        //then
        assertEquals(expectedSubmissionApiWithRejectReasons(now), actual);
    }

    private Submission submissionDataEntity(LocalDateTime now) {
        return Submission.builder()
                .withId(SUBMISSION_ID)
                .withConfirmationReference(CONFIRMATION_REFERENCE)
                .withPresenter(new Presenter(EMAIL_ADDRESS))
                .withCompany(new Company(COMPANY_NUMBER, COMPANY_NAME))
                .withStatus(SubmissionStatus.ACCEPTED)
                .withPaymentSessions(PAYMENT_SESSIONS)
                .withFeeOnSubmission(FEE_ON_SUBMISSION)
                .withConfirmAuthorised(CONFIRM_AUTHORISED)
                .withFormDetails(FormDetails.builder()
                        .withBarcode(BARCODE)
                        .withFormType(FORM_TYPE)
                        .withFileDetailsList(Collections.singletonList(FileDetails.builder()
                                .withFileId(FILE_ID)
                                .withFileName(FILENAME)
                                .withFileSize(FILE_SIZE)
                                .withIncorporationComponent("Inc component")
                                .withConvertedFileId(CONVERTED_FILE_ID)
                                .withConversionStatus(FileConversionStatus.CONVERTED)
                                .withLastModifiedAt(now)
                                .build()))
                        .build())
                .withCreatedAt(now)
                .withSubmittedAt(now)
                .withLastModifiedAt(now)
                .build();
    }

    private SubmissionApi expectedSubmissionApi(LocalDateTime now) {
        return new SubmissionApi(SUBMISSION_ID, CONFIRMATION_REFERENCE, new PresenterApi(EMAIL_ADDRESS),
            new CompanyApi(COMPANY_NUMBER, COMPANY_NAME), SubmissionStatus.ACCEPTED, PAYMENT_SESSIONS,
            FEE_ON_SUBMISSION, CONFIRM_AUTHORISED, new SubmissionFormApi(BARCODE, FORM_TYPE, new FileDetailListApi(
            Collections.singletonList(
                new FileDetailApi(FILE_ID, FILENAME, FILE_SIZE, "Inc component", CONVERTED_FILE_ID, FileConversionStatus.CONVERTED, null,
                    now)))), now, now, now, null);
    }

    private SubmissionApi expectedSubmissionApiWithRejectReasons(LocalDateTime now) {
        return new SubmissionApi(SUBMISSION_ID, CONFIRMATION_REFERENCE, new PresenterApi(EMAIL_ADDRESS),
            new CompanyApi(COMPANY_NUMBER, COMPANY_NAME), SubmissionStatus.ACCEPTED, PAYMENT_SESSIONS,
            FEE_ON_SUBMISSION, CONFIRM_AUTHORISED, new SubmissionFormApi(BARCODE, FORM_TYPE, new FileDetailListApi(
            Collections.singletonList(
                new FileDetailApi(FILE_ID, FILENAME, FILE_SIZE, "Inc component", CONVERTED_FILE_ID, FileConversionStatus.CONVERTED, null,
                    now)))), now, now, now, Collections.singletonList(new RejectReasonApi(REJECT_REASON)));
    }
}
