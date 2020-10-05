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
    private static final LocalDate REPORT_DATE = LocalDate.of(2020, 8, 31);
    private static final FormDetails FORM_SCOT_FEE = new FormDetails(null, "SQP1", null);
    private static final FormDetails FORM_NON_SCOT_FEE = new FormDetails(null, "CS01", null);
    private static final String REPORT_SCOT = "'EFS_ScottishPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final String REPORT_PAID = "'EFS_PaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final String REPORT_FAILED = "'EFS_FailedPaymentTransactions_'yyyy-MM-dd'.csv'";
    private static final List<String> SCOT_FORM_LIST = Arrays.asList("SQPCS01", "SLPCS01", "SQP1", "LP5(S)", "LP7(S)");
    private static final String BUCKET_NAME = "TEST_BUCKET";
    private static final String ENV_NAME = "TEST_ENV";
    protected static final String SUCCESS_CSV_CONTENT =
        "submissionId,customerRef,userEmail,submittedAt,amountPaid,paymentRef,formType,companyNumber\n"
            + "SCOT_FEE,REF_SF,presenter@nomail.net,2020-08-31T10:10:10,10,PAY_SF,SQP1,00000000\n"
            + "NON_SCOT_FEE,REF_NSF,presenter@nomail.net,2020-08-31T12:12:12,10,PAY_NSF,CS01,00000000\n";
    protected static final String FAILED_CSV_CONTENT =
        "submissionId,customerRef,userEmail,submittedAt,amountPaid,paymentRef,formType,companyNumber\n"
            + "SCOT_FAILED_FEE,REF_SFF,presenter@nomail.net,2020-08-31T11:11:11,10,PAY_SFF,SQP1,00000000\n"
            + "FAILED_FEE,REF_FF,presenter@nomail.net,2020-08-31T13:13:13,10,PAY_FF,CS01,00000000\n";


    private PaymentReportServiceImpl spyService;

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
    private Submission submissionNSF;
    private Submission submissionNSFF;

    @BeforeEach
    void setUp() {
        final Clock clock =
            Clock.fixed(REPORT_DATE.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        spyService = spy(new PaymentReportServiceImpl(emailService,
            new ReportQueryServiceImpl(submissionRepository, reportMapper), outputStreamWriterFactory, s3ClientService,
            clock, BUCKET_NAME));
        ReflectionTestUtils.setField(spyService, "scotlandReportPattern", REPORT_SCOT);
        ReflectionTestUtils.setField(spyService, "scotlandForms", SCOT_FORM_LIST);
        ReflectionTestUtils.setField(spyService, "financeReportPattern", REPORT_PAID);
        ReflectionTestUtils.setField(spyService, "failedTransactionsFinanceReportPattern", REPORT_FAILED);
        ReflectionTestUtils.setField(spyService, "reportPeriodDaysBeforeToday", 1);

        final Company company = new Company("00000000", "Testing Services");
        final Presenter presenter = new Presenter("presenter@nomail.net");
        final Submission.Builder builder = new Submission.Builder();

        submissionSF = builder.withId("SCOT_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SF").withPresenter(presenter)
            .withSubmittedAt(REPORT_DATE.atTime(10, 10, 10)).withFeeOnSubmission("10").withPaymentReference("PAY_SF")
            .withStatus(SubmissionStatus.SUBMITTED).build();
        submissionSFF = builder.withId("SCOT_FAILED_FEE").withCompany(company).withFormDetails(FORM_SCOT_FEE)
            .withConfirmationReference("REF_SFF").withPresenter(presenter)
            .withSubmittedAt(REPORT_DATE.atTime(11, 11, 11)).withFeeOnSubmission("10").withPaymentReference("PAY_SFF")
            .withStatus(SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER).build();
        submissionNSF = builder.withId("NON_SCOT_FEE").withCompany(company).withFormDetails(FORM_NON_SCOT_FEE)
            .withConfirmationReference("REF_NSF").withPresenter(presenter)
            .withSubmittedAt(REPORT_DATE.atTime(12, 12, 12)).withFeeOnSubmission("10").withPaymentReference("PAY_NSF")
            .withStatus(SubmissionStatus.REJECTED).build();
        submissionNSFF = builder.withId("FAILED_FEE").withCompany(company).withFormDetails(FORM_NON_SCOT_FEE)
            .withConfirmationReference("REF_FF").withPresenter(presenter)
            .withSubmittedAt(REPORT_DATE.atTime(13, 13, 13)).withFeeOnSubmission("10").withPaymentReference("PAY_FF")
            .withStatus(SubmissionStatus.REJECTED_BY_VIRUS_SCAN).build();
    }

    @Test
    void sendScotlandPaymentReport() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES,
            Collections.singletonList(submissionSF));
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        final String reportName = "EFS_ScottishPaymentTransactions_2020-08-31.csv";

        final String fileLink = "link.to.uploaded.file";

        when(s3ClientService.getResourceId(anyString())).thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + reportName, BUCKET_NAME)).thenReturn(fileLink);

        spyService.sendScotlandPaymentReport();

        verify(emailService).sendPaymentReportEmail(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getFileLink(), is(fileLink));
        assertThat(emailCaptor.getValue().getFileName(), is(reportName.replace(".csv", "")));
    }

    @Test
    void sendScotlandPaymentReportWhenWriterFails() throws IOException {
        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, Collections.emptyList());
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenReturn(outputStreamWriter);
        doThrow(new IOException("expected failure")).when(outputStreamWriter)
            .write(any(char[].class), anyInt(), anyInt());

        final IOException exception = assertThrows(IOException.class, () -> spyService.sendScotlandPaymentReport());

        assertThat(exception.getMessage(), is("expected failure"));
    }

    @Test
    void sendFinancePaymentReports() throws IOException {
        final String successReportName = "EFS_PaymentTransactions_2020-08-31.csv";
        final String failedReportName = "EFS_FailedPaymentTransactions_2020-08-31.csv";
        final String successFileLink = "link.to.uploaded.success.file";
        final String failedFileLink = "link.to.uploaded.failed.file";

        expectFindPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES,
            Arrays.asList(submissionSF, submissionNSF));
        expectFindPaidSubmissions(PaymentReportServiceImpl.FAILED_STATUSES,
            Arrays.asList(submissionSFF, submissionNSFF));
        expectReportUpload(successFileLink, successReportName, failedFileLink, failedReportName);
        when(outputStreamWriterFactory.createFor(any(BufferedOutputStream.class))).thenCallRealMethod();

        spyService.sendFinancePaymentReports();

        verify(emailService, times(2)).sendPaymentReportEmail(emailCaptor.capture());

        final List<PaymentReportEmailModel> values = emailCaptor.getAllValues();

        assertThat(values.get(0).getFileLink(), is(successFileLink));
        assertThat(values.get(0).getFileName(), is(successReportName.replace(".csv", "")));
        assertThat(values.get(1).getFileLink(), is(failedFileLink));
        assertThat(values.get(1).getFileName(), is(failedReportName.replace(".csv", "")));

    }

    private void expectReportUpload(final String successFileLink, final String successReportName,
        final String failedFileLink, final String failedReportName) {

        when(s3ClientService.getResourceId(anyString())).thenAnswer(invocation -> ENV_NAME + "/" + invocation.getArgument(0));
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + successReportName, BUCKET_NAME)).thenReturn(successFileLink);
        when(s3ClientService.generateFileLink(ENV_NAME + "/" + failedReportName, BUCKET_NAME)).thenReturn(failedFileLink);
    }

    private void expectFindPaidSubmissions(ImmutableSet<SubmissionStatus> statuses, List<Submission> mappedList) {
        when(submissionRepository.findPaidSubmissions(statuses, REPORT_DATE)).thenReturn(mappedList);
        mappedList.forEach(s -> when(reportMapper.map(s)).thenReturn(buildTransaction(s)));
    }

    private PaymentTransaction buildTransaction(final Submission submission) {
        return new PaymentTransactionBuilder().withFormType(submission.getFormDetails().getFormType())
            .withSubmissionId(submission.getId()).withAmountPaid(submission.getFeeOnSubmission())
            .withCompanyNumber(submission.getCompany().getCompanyNumber())
            .withCustomerRef(submission.getConfirmationReference()).withPaymentRef(submission.getPaymentReference())
            .withSubmittedAt(submission.getSubmittedAt()).withUserEmail(submission.getPresenter().getEmail()).build();
    }
}