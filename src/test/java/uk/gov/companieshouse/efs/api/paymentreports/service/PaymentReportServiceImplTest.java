package uk.gov.companieshouse.efs.api.paymentreports.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
    private static final LocalDate START_DATE = LocalDate.of(2020, 8, 31);
    private static final LocalTime NOW_TIME = LocalTime.of(1, 1, 1);
    private static final FormDetails FORM_SCOT_FEE = new FormDetails(null, "SQP1", null);
    private static final FormDetails FORM_NON_SCOT_FEE = new FormDetails(null, "CS01", null);
    private static final FormDetails FORM_SH19_FEE = new FormDetails(null, "SH19", null);
    private static final FormDetails FORM_SH19_SAMEDAY_FEE = new FormDetails(null, "SH19_SAMEDAY", null);
    private static final String REPORT_SCOT = "'EFS_ScottishPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final String REPORT_FAILED = "'EFS_FailedPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final String REPORT_SH19 = "'EFS_SH19Transactions_'yyyy-MM-dd'.csv'";
    private static final List<String> SCOT_FORM_LIST = Arrays.asList("SQPCS01", "SLPCS01", "SQP1", "LP5(S)", "LP7(S)");
    private static final String BUCKET_NAME = "TEST_BUCKET";
    private static final String ENV_NAME = "TEST_ENV";

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
    private Submission submissionNSFF;
    private Submission submissionSH19;
    private Submission submissionSH19_SAMEDAY;

    @BeforeEach
    void setUp() {
        testService = createTestServiceSpy(1);

        final Company company = new Company("00000000", "Testing Services");
        final Presenter presenter = new Presenter("presenter@nomail.net");
        final Submission.Builder builder = new Submission.Builder();

        submissionSF = builder.withId("SCOT_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SF").withPresenter(presenter).withSubmittedAt(START_DATE.atTime(10, 10, 10))
            .withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_SF"))
            .withStatus(SubmissionStatus.SUBMITTED).build();
        submissionSFF = builder.withId("SCOT_FAILED_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SFF").withPresenter(presenter)
            .withSubmittedAt(START_DATE.atTime(11, 11, 11)).withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_SFF"))
            .withStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER).build();
        submissionNSFF = builder.withId("FAILED_FEE").withCompany(company).withFormDetails(FORM_NON_SCOT_FEE)
            .withConfirmationReference("REF_FF").withPresenter(presenter).withSubmittedAt(START_DATE.atTime(13, 13, 13))
            .withFeeOnSubmission("10")
            .withPaymentSessions(createPaymentSessions("PAY_FF"))
            .withStatus(SubmissionStatus.REJECTED_BY_VIRUS_SCAN).build();
        submissionSH19 = builder.withId("FAILED_FEE").withCompany(company).withFormDetails(FORM_SH19_FEE)
            .withConfirmationReference("REF_FF").withPresenter(presenter).withSubmittedAt(START_DATE.atTime(13, 13, 13))
            .withFeeOnSubmission("50")
            .withPaymentSessions(createPaymentSessions("PAY_FF"))
            .withStatus(SubmissionStatus.REJECTED_BY_VIRUS_SCAN).build();
        submissionSH19_SAMEDAY = builder.withId("FAILED_FEE").withCompany(company).withFormDetails(FORM_SH19_SAMEDAY_FEE)
            .withConfirmationReference("REF_FF").withPresenter(presenter).withSubmittedAt(START_DATE.atTime(13, 13, 13))
            .withFeeOnSubmission("50")
            .withPaymentSessions(createPaymentSessions("PAY_FF"))
            .withStatus(SubmissionStatus.REJECTED_BY_VIRUS_SCAN).build();
    }

    private PaymentReportServiceImpl createTestServiceSpy(final int reportPeriodDaysBeforeToday) {
        final Clock clock = getFixedClock(reportPeriodDaysBeforeToday);

        PaymentReportServiceImpl spyService = spy(new PaymentReportServiceImpl(emailService,
            new ReportQueryServiceImpl(submissionRepository, reportMapper), outputStreamWriterFactory, s3ClientService,
            clock, BUCKET_NAME));
        ReflectionTestUtils.setField(spyService, "scotlandReportPattern", REPORT_SCOT);
        ReflectionTestUtils.setField(spyService, "scotlandForms", SCOT_FORM_LIST);
        ReflectionTestUtils.setField(spyService, "failedTransactionsFinanceReportPattern", REPORT_FAILED);
        ReflectionTestUtils.setField(spyService, "sh19FinanceReportPattern", REPORT_SH19);
        ReflectionTestUtils.setField(spyService, "reportPeriodDaysBeforeToday", reportPeriodDaysBeforeToday);

        return spyService;
    }

    private Clock getFixedClock(final int daysAfterStartDate) {
        return Clock.fixed(START_DATE.plusDays(daysAfterStartDate).atTime(NOW_TIME).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    }

    @Test
    void sendScotlandPaymentReport() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE,
            Collections.singletonList(submissionSF));
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        final String reportName = "EFS_ScottishPaymentTransactions_2020-08-31.csv";
        final String fileLink = "link.to.uploaded.file";

        when(s3ClientService.getResourceId(anyString()))
            .thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + reportName, BUCKET_NAME)).thenReturn(fileLink);

        testService.sendScotlandPaymentReport();

        verify(emailService).sendPaymentReportEmail(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getFileLink(), is(fileLink));
        assertThat(emailCaptor.getValue().getFileName(), is(reportName.replace(".csv", "")));
    }

    @Test
    void sendConsecutiveScotlandPaymentReportsThenPeriodsNotOverlapping() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE,
            Collections.singletonList(submissionSF));
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        // on the first report day...
        testService.sendScotlandPaymentReport();

        verify(submissionRepository)
            .findPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE, START_DATE.plusDays(1));

        // on the next day...
        final Clock nextDay = getFixedClock(2);

        ReflectionTestUtils.setField(testService, "clock", nextDay);

        testService.sendScotlandPaymentReport();

        verify(submissionRepository)
            .findPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE.plusDays(1),
                START_DATE.plusDays(2));
    }

    @Test
    void sendOlderScotlandPaymentReport() throws IOException {
        testService = createTestServiceSpy(5); // NOW is 5 days *after* TEST_DATE
        sendScotlandPaymentReport();
    }

    @Test
    void sendScotlandPaymentReportWhenWriterFails() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE, Collections.emptyList());
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenReturn(outputStreamWriter);
        doThrow(new IOException("expected failure")).when(outputStreamWriter)
            .write(any(char[].class), anyInt(), anyInt());

        final IOException exception = assertThrows(IOException.class, () -> testService.sendScotlandPaymentReport());

        assertThat(exception.getMessage(), is("expected failure"));
    }

    @Test
    void sendFinancePaymentFailureReportWithSh19() throws IOException {
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String failedFileLink = "link.to.uploaded.failed.file";
        final String sh19ReportName = "EFS_SH19Transactions_2020-08-31.csv";
        final String successFileLink = "link.to.uploaded.success.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE,
            Arrays.asList(submissionSFF, submissionNSFF, submissionSH19));
        expectReportUpload(successFileLink, sh19ReportName);
        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES, START_DATE,
            Arrays.asList(submissionSFF, submissionNSFF));
        expectReportUpload(failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        testService.sendFinancePaymentReports();

        verify(emailService, times(2)).sendPaymentReportEmail(emailCaptor.capture());

        final List<PaymentReportEmailModel> values = emailCaptor.getAllValues();

        assertThat(values.get(0).getHasNoPaymentTransactions(), is(false));
        assertThat(values.get(0).getFileLink(), is(successFileLink));
        assertThat(values.get(0).getFileName(), is(sh19ReportName.replace(".csv", "")));
        assertThat(values.get(1).getFileLink(), is(failedFileLink));
        assertThat(values.get(1).getFileName(), is(failedReportName.replace(".csv", "")));

    }

    @Test
    void sendFinancePaymentFailureReportWithSh19_Sameday() throws IOException {
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String failedFileLink = "link.to.uploaded.failed.file";
        final String sh19ReportName = "EFS_SH19Transactions_2020-08-31.csv";
        final String successFileLink = "link.to.uploaded.success.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE,
            Arrays.asList(submissionSFF, submissionNSFF, submissionSH19_SAMEDAY));
        expectReportUpload(successFileLink, sh19ReportName);
        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES, START_DATE,
            Arrays.asList(submissionSFF, submissionNSFF));
        expectReportUpload(failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        testService.sendFinancePaymentReports();

        verify(emailService, times(2)).sendPaymentReportEmail(emailCaptor.capture());

        final List<PaymentReportEmailModel> values = emailCaptor.getAllValues();

        assertThat(values.get(0).getHasNoPaymentTransactions(), is(false));
        assertThat(values.get(0).getFileLink(), is(successFileLink));
        assertThat(values.get(0).getFileName(), is(sh19ReportName.replace(".csv", "")));
        assertThat(values.get(1).getHasNoPaymentTransactions(), is(false));
        assertThat(values.get(1).getFileLink(), is(failedFileLink));
        assertThat(values.get(1).getFileName(), is(failedReportName.replace(".csv", "")));

    }

    @Test
    void sendFinancePaymentFailureReportWhenEmpty() throws IOException {
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String failedFileLink = "link.to.uploaded.failed.file";
        final String sh19ReportName = "EFS_SH19Transactions_2020-08-31.csv";
        final String successFileLink = "link.to.uploaded.success.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, START_DATE, Collections.emptyList());
        expectReportUpload(successFileLink, sh19ReportName);
        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES, START_DATE, Collections.emptyList());
        expectReportUpload(failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        testService.sendFinancePaymentReports();

        verify(emailService, times(2)).sendPaymentReportEmail(emailCaptor.capture());

        final List<PaymentReportEmailModel> values = emailCaptor.getAllValues();

        assertThat(values.get(0).getHasNoPaymentTransactions(), is(true));
        assertThat(values.get(0).getFileLink(), is(successFileLink));
        assertThat(values.get(0).getFileName(), is(sh19ReportName.replace(".csv", "")));
        assertThat(values.get(1).getHasNoPaymentTransactions(), is(true));
        assertThat(values.get(1).getFileLink(), is(failedFileLink));
        assertThat(values.get(1).getFileName(), is(failedReportName.replace(".csv", "")));

    }

    private void expectReportUpload(final String failedFileLink, final String failedReportName) {
        when(s3ClientService.getResourceId(anyString()))
            .thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + failedReportName, BUCKET_NAME))
            .thenReturn(failedFileLink);
    }

    private void expectFindPaidSubmissions(ImmutableSet<SubmissionStatus> statuses, final LocalDate startDate,
        List<Submission> mappedList) {
        // report period should be 1 day long
        when(submissionRepository.findPaidSubmissions(statuses, startDate, startDate.plusDays(1)))
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
        return new SessionListApi(Collections.singletonList(
            new SessionApi(sessionRef, sessionRef + "-state", sessionRef + "-status")));
    }
}