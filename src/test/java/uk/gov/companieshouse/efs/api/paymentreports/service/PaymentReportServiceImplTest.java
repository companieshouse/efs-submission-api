package uk.gov.companieshouse.efs.api.paymentreports.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.events.service.S3ClientService;
import uk.gov.companieshouse.efs.api.paymentreports.mapper.PaymentReportMapper;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransactionBuilder;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class PaymentReportServiceImplTest {
    private static final LocalDate TEST_DATE = LocalDate.of(2020, 8, 31);
    private static final LocalTime NOW_TIME = LocalTime.of(1, 1, 1);
    private static final FormDetails FORM_SCOT_FEE = new FormDetails(null, "SQP1", null);
    private static final FormDetails FORM_NON_SCOT_FEE = new FormDetails(null, "CS01", null);
    private static final String REPORT_SCOT = "'EFS_ScottishPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final String REPORT_FAILED = "'EFS_FailedPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final List<String> SCOT_FORM_LIST = Arrays.asList("SQPCS01", "SLPCS01", "SQP1", "LP5(S)", "LP7(S)");
    private static final String BUCKET_NAME = "TEST_BUCKET";
    private static final String ENV_NAME = "TEST_ENV";
    protected static final String FAILED_CSV_CONTENT =
        "submissionId,customerRef,userEmail,submittedAt,amountPaid,paymentRef,formType,companyNumber\n"
            + "SCOT_FAILED_FEE,REF_SFF,presenter@nomail.net,2020-08-31T11:11:11,10,PAY_SFF,SQP1,00000000\n"
            + "FAILED_FEE,REF_FF,presenter@nomail.net,2020-08-31T13:13:13,10,PAY_FF,CS01,00000000\n";


    private PaymentReportServiceImpl testService;

    @Mock
    private EmailService emailService;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private PaymentReportMapper reportMapper;
    @Mock
    private S3ClientService s3ClientService;
    @Mock
    private OutputStreamWriterFactory outputStreamWriterFactory;
    @Mock
    private OutputStreamWriter outputStreamWriter;
    @Captor
    private ArgumentCaptor<PaymentReportEmailModel> emailCaptor;

    private Submission submissionSF;
    private Submission submissionSFF;
    private Submission submissionOSF;
    private Submission submissionNSFF;

    @BeforeEach
    void setUp() {
        testService = createTestServiceSpy(1);

        final Company company = new Company("00000000", "Testing Services");
        final Presenter presenter = new Presenter("presenter@nomail.net");
        final Submission.Builder builder = new Submission.Builder();

        submissionSF = builder.withId("SCOT_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SF").withPresenter(presenter)
            .withSubmittedAt(TEST_DATE.atTime(10, 10, 10)).withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_SF"))
            .withStatus(SubmissionStatus.SUBMITTED).build();
        submissionSFF = builder.withId("SCOT_FAILED_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SFF").withPresenter(presenter)
            .withSubmittedAt(TEST_DATE.atTime(11, 11, 11)).withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_SFF"))
            .withStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER).build();
        submissionNSFF = builder.withId("FAILED_FEE").withCompany(company).withFormDetails(FORM_NON_SCOT_FEE)
            .withConfirmationReference("REF_FF").withPresenter(presenter)
            .withSubmittedAt(TEST_DATE.atTime(13, 13, 13)).withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_FF"))
            .withStatus(SubmissionStatus.REJECTED_BY_VIRUS_SCAN).build();
    }

    private PaymentReportServiceImpl createTestServiceSpy(final int reportPeriodDaysBeforeToday) {
        final Clock clock =
            Clock.fixed(TEST_DATE.plusDays(reportPeriodDaysBeforeToday).atTime(NOW_TIME).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        PaymentReportServiceImpl spyService = spy(new PaymentReportServiceImpl(emailService,
            new ReportQueryServiceImpl(submissionRepository, reportMapper), outputStreamWriterFactory, s3ClientService,
            clock, BUCKET_NAME));
        ReflectionTestUtils.setField(spyService, "scotlandReportPattern", REPORT_SCOT);
        ReflectionTestUtils.setField(spyService, "scotlandForms", SCOT_FORM_LIST);
        ReflectionTestUtils.setField(spyService, "failedTransactionsFinanceReportPattern", REPORT_FAILED);
        ReflectionTestUtils.setField(spyService, "reportPeriodDaysBeforeToday", reportPeriodDaysBeforeToday);

        return spyService;
    }

    @Test
    void sendScotlandPaymentReport() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES,
            Collections.singletonList(submissionSF), TEST_DATE);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        final String reportName = "EFS_ScottishPaymentTransactions_2020-08-31.csv";
        final String fileLink = "link.to.uploaded.file";

        when(s3ClientService.getResourceId(anyString())).thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + reportName, BUCKET_NAME)).thenReturn(fileLink);

        testService.sendScotlandPaymentReport();

        verify(emailService).sendPaymentReportEmail(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getFileLink(), is(fileLink));
        assertThat(emailCaptor.getValue().getFileName(), is(reportName.replace(".csv", "")));
    }

    @Test
    void sendOlderScotlandPaymentReport() throws IOException {
        testService = createTestServiceSpy(5); // NOW is 5 days *after* TEST_DATE
        sendScotlandPaymentReport();
    }

    @Test
    void sendScotlandPaymentReportWhenWriterFails() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, Collections.emptyList(), TEST_DATE);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenReturn(outputStreamWriter);
        doThrow(new IOException("expected failure")).when(outputStreamWriter)
            .write(any(char[].class), anyInt(), anyInt());

        final IOException exception = assertThrows(IOException.class, () -> testService.sendScotlandPaymentReport());

        assertThat(exception.getMessage(), is("expected failure"));
    }

    @Test
    void sendFinancePaymentFailureReport() throws IOException {
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String failedFileLink = "link.to.uploaded.failed.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES,
            Arrays.asList(submissionSFF, submissionNSFF), TEST_DATE);
        expectReportUpload(failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        testService.sendFinancePaymentReports();

        verify(emailService).sendPaymentReportEmail(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getFileLink(), is(failedFileLink));
        assertThat(emailCaptor.getValue().getFileName(), is(failedReportName.replace(".csv", "")));

    }

    @Test
    void sendFinancePaymentFailureReportWhenEmpty() throws IOException {
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String failedFileLink = "link.to.uploaded.failed.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES,
            Collections.emptyList(), TEST_DATE);
        expectReportUpload(failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        testService.sendFinancePaymentReports();

        verify(emailService).sendPaymentReportEmail(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getFileLink(), is(failedFileLink));
        assertThat(emailCaptor.getValue().getFileName(), is(failedReportName.replace(".csv", "")));

    }

    private void expectReportUpload(final String failedFileLink, final String failedReportName) {
        when(s3ClientService.getResourceId(anyString())).thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + failedReportName, BUCKET_NAME)).thenReturn(failedFileLink);
    }

    private void expectFindPaidSubmissions(ImmutableSet<SubmissionStatus> statuses, List<Submission> mappedList,
        final LocalDate reportStartDate) {
        // report period should be 1 day long
        when(submissionRepository.findPaidSubmissions(statuses, reportStartDate, reportStartDate.plusDays(1)))
            .thenReturn(mappedList);
        mappedList.forEach(s -> when(reportMapper.map(s)).thenReturn(buildTransaction(s)));
    }

    private PaymentTransaction buildTransaction(final Submission submission) {
        return new PaymentTransactionBuilder().withFormType(submission.getFormDetails().getFormType())
            .withSubmissionId(submission.getId()).withAmountPaid(submission.getFeeOnSubmission())
            .withCompanyNumber(submission.getCompany().getCompanyNumber())
            .withCustomerRef(submission.getConfirmationReference()).withPaymentRef(
                submission.getPaymentSessions().stream().findFirst().map(SessionApi::getSessionId).orElse(null))
            .withSubmittedAt(submission.getSubmittedAt()).withUserEmail(submission.getPresenter().getEmail()).build();
    }

    private SessionListApi createPaymentSessions(final String sessionRef) {
        return new SessionListApi(Collections.singletonList(new SessionApi(sessionRef, sessionRef + "-state")));
    }
}