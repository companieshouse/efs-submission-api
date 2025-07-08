package uk.gov.companieshouse.efs.api.submissions.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

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
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class SubmissionApiMapperTest {

    private static final Instant FIXED_NOW = Instant.parse("2022-01-01T12:00:00Z");
    public static final LocalDateTime LOCAL_FIXED_NOW = LocalDateTime.ofInstant(FIXED_NOW, ZoneOffset.UTC);
    private SubmissionApi submissionApi;
    private SubmissionApiMapper testMapper;

    @BeforeEach
    void setUp() {
        submissionApi = new SubmissionApi("Id", "confirmation reference", new PresenterApi("email"),
                new CompanyApi("company number", "company name"), SubmissionStatus.OPEN, new SessionListApi(
                Collections.singletonList(new SessionApi("session Id", "session state", "session Status"))),
                "fee on submission", true, new SubmissionFormApi("barcode", "form type", new FileDetailListApi(
                Collections.singletonList(new FileDetailApi("File id", "file name", 10000L, "converted file id",
                        FileConversionStatus.CONVERTED, 10, LOCAL_FIXED_NOW)))), LOCAL_FIXED_NOW, LOCAL_FIXED_NOW.plusMinutes(1),
                LOCAL_FIXED_NOW.plusMinutes(2), Collections.singletonList(new RejectReasonApi("reason")));

        testMapper = new SubmissionApiMapper();
    }

    @Test
    void map() {
        Submission submission = Submission.builder()
                .withId("Id")
                .withConfirmationReference("confirmation reference")
                .withPresenter(new Presenter("email"))
                .withCompany(new Company("company number", "company name"))
                .withStatus(SubmissionStatus.OPEN)
                .withPaymentSessions(new SessionListApi(
                        Collections.singletonList(new SessionApi("session Id", "session state", "session Status"))))
                .withFeeOnSubmission("fee on submission")
                .withConfirmAuthorised(true)
                .withFormDetails(FormDetails.builder()
                        .withBarcode("barcode")
                        .withFormType("form type")
                        .withFileDetailsList(Collections.singletonList(FileDetails.builder()
                                .withFileId("File id")
                                .withFileName("file name")
                                .withFileSize(10000L)
                                .withConvertedFileId("converted file id")
                                .withConversionStatus(FileConversionStatus.CONVERTED)
                                .withNumberOfPages(10)
                                .withLastModifiedAt(LOCAL_FIXED_NOW)
                                .build()))
                        .build())
                .withSubmittedAt(LOCAL_FIXED_NOW.plusMinutes(1))
                .withLastModifiedAt(LOCAL_FIXED_NOW.plusMinutes(2))
                .withCreatedAt(LOCAL_FIXED_NOW)
                .withChipsRejectReasons(Collections.singletonList(new RejectReason("reason")))
                .build();

        final Submission mappedSubmission = testMapper.map(submissionApi);
        System.out.println("expect=" + ReflectionToStringBuilder.toString(mappedSubmission, RecursiveToStringStyle.MULTI_LINE_STYLE));
        System.out.println("actual=" + ReflectionToStringBuilder.toString(submission, RecursiveToStringStyle.MULTI_LINE_STYLE));
        assertThat(mappedSubmission, is(equalTo(submission)));
    }

    @Test
    void mapNullRejectReasons() {
        submissionApi.setRejectReasons(null);

        final Submission mappedSubmission = testMapper.map(submissionApi);

        assertThat(mappedSubmission.getChipsRejectReasons(), is(nullValue()));
    }
}