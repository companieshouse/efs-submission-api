package uk.gov.companieshouse.efs.api.paymentreports.service;

import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.ACCEPTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSED_BY_EMAIL;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSING;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.READY_TO_SUBMIT;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED_BY_VIRUS_SCAN;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SENT_TO_FES;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SUBMITTED;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.email.EmailService;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;
import uk.gov.companieshouse.efs.api.events.service.S3ClientService;
import uk.gov.companieshouse.efs.api.paymentreports.mapper.PaymentReportMapper;
import uk.gov.companieshouse.efs.api.paymentreports.model.PaymentTransaction;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.repository.SubmissionRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class PaymentReportServiceImpl implements PaymentReportService {

    public static final ImmutableSet<SubmissionStatus> SUCCESSFUL_STATUSES =
        Sets.immutableEnumSet(SUBMITTED, PROCESSING, PROCESSED_BY_EMAIL, READY_TO_SUBMIT, ACCEPTED, REJECTED,
            SENT_TO_FES);

    public static final ImmutableSet<SubmissionStatus> FAILED_STATUSES =
        Sets.immutableEnumSet(REJECTED_BY_DOCUMENT_CONVERTER, REJECTED_BY_VIRUS_SCAN);

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final Clock clock;

    @Value("${report.period.days.before.today}")
    private int reportPeriodDaysBeforeToday;

    @Value("${report.filename.pattern.scotland}")
    private String scotlandReportPattern;

    @Value("${report.filename.pattern.failed.transactions}")
    private String failedTransactionsFinanceReportPattern;

    @Value("${report.filename.pattern.sh19.transactions}")
    private String sh19FinanceReportPattern;

    @Value("${scotland.payment.form.types}")
    private List<String> scotlandForms;

    private final EmailService emailService;

    private final PaymentReportMapper paymentReportMapper;

    private final OutputStreamWriterFactory outputStreamWriterFactory;

    private final SubmissionRepository repository;

    private final S3ClientService s3ClientService;

    private final String paymentReportBucketName;

    @Autowired
    public PaymentReportServiceImpl(final EmailService emailService, final ReportQueryServiceImpl reportQueryService,
        final OutputStreamWriterFactory outputStreamWriterFactory, S3ClientService s3ClientService,
        final Clock clock, @Qualifier("paymentReportBucketName") String paymentReportBucketName) {

        this.emailService = emailService;
        this.paymentReportMapper = reportQueryService.getPaymentReportMapper();
        this.repository = reportQueryService.getRepository();
        this.outputStreamWriterFactory = outputStreamWriterFactory;
        this.s3ClientService = s3ClientService;
        this.clock = clock;
        this.paymentReportBucketName = paymentReportBucketName;
    }

    @Override
    public void sendScotlandPaymentReport() throws IOException {
        List<PaymentTransaction> scottishPaymentTransactions =
            findPaymentTransactions(SUCCESSFUL_STATUSES).stream().filter(p -> scotlandForms.contains(p.getFormType()))
                .toList();

        createReport(scotlandReportPattern, scottishPaymentTransactions);
    }

    @Override
    public void sendFinancePaymentReports() throws IOException {
        List<PaymentTransaction> sh19Transactions =
            findPaymentTransactions(SUCCESSFUL_STATUSES).stream()
                .filter(p -> (p.getFormType().startsWith("SH19")))
                .toList();
        createReport(sh19FinanceReportPattern, sh19Transactions);
        createReport(failedTransactionsFinanceReportPattern, findPaymentTransactions(FAILED_STATUSES));
    }

    private List<PaymentTransaction> findPaymentTransactions(ImmutableSet<SubmissionStatus> statuses) {
        final LocalDate startDate = LocalDate.now(clock).minusDays(reportPeriodDaysBeforeToday);
        final LocalDate endDate = startDate.plusDays(1); // report period should be 1 day long
        final List<Submission> submissions = repository.findPaidSubmissions(statuses, startDate, endDate);

        return submissions.stream().map(paymentReportMapper::map).toList();
    }

    @Override
    public String generateCsvFileContent(List<PaymentTransaction> paymentTransactions) throws IOException {
        final CsvMapper csvMapper = CsvMapper.builder().disable(
            MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build();
        final CsvSchema csvSchema = csvMapper.schemaFor(PaymentTransaction.class).withHeader();
        final ByteArrayOutputStream content = new ByteArrayOutputStream();

        try (final OutputStreamWriter woStream = outputStreamWriterFactory.createFor(
            new BufferedOutputStream(content))) {
            csvMapper.writer(csvSchema).writeValue(woStream, paymentTransactions);
            return content.toString(StandardCharsets.UTF_8.toString());
        } catch (IOException ex) {
            LOGGER.error("Error when creating Payment csv content: ", ex);
            throw ex;
        }
    }

    private String formatReportName(final LocalDate reportDate, final String reportPattern) {
        return DateTimeFormatter.ofPattern(reportPattern).format(reportDate);
    }

    private void createReport(String reportPattern, List<PaymentTransaction> paymentTransactions) throws IOException {
        String reportName =
            formatReportName(LocalDate.now(clock).minusDays(reportPeriodDaysBeforeToday), reportPattern);
        String csvContent = generateCsvFileContent(paymentTransactions);
        s3ClientService.uploadToS3(reportName, csvContent, paymentReportBucketName);
        LOGGER.info(String.format("Sending payment report email [%s] to Finance", reportName));

        final boolean hasNoPaymentTransactions = paymentTransactions.isEmpty();

        emailService.sendPaymentReportEmail(
            new PaymentReportEmailModel(s3ClientService.generateFileLink(s3ClientService.getResourceId(reportName),
                paymentReportBucketName), reportName.replace(".csv", ""), hasNoPaymentTransactions));
    }

}
