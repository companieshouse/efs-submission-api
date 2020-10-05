package uk.gov.companieshouse.efs.api.submissions.repository;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.paymentreports.service.PaymentReportServiceImpl;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
public class SubmissionRepositoryImplTest {

    private static final String ID = "_id";
    private static final String FORM_BARCODE = "form.barcode";
    private static final String STATUS = "status";
    private static final String SUBMISSIONS_COLLECTION = "submissions";
    private static final String SUBMITTED_AT = "submitted_at";
    private static final String FEE_ON_SUBMISSION = "fee_on_submission";
    private static final String LAST_MODIFIED_AT = "last_modified_at";

    private SubmissionRepositoryImpl repository;

    private static final String SUBMISSION_ID = "aaaabbbbccccdddd";
    private static final String BARCODE = "123456";

    @Mock
    private MongoTemplate template;

    @Mock
    private CurrentTimestampGenerator timestampGenerator;

    @Mock
    private Submission submission;

    private LocalDateTime localDateTime;

    @BeforeEach
    public void setUp() {
        this.localDateTime = LocalDateTime.now();
        repository = new SubmissionRepositoryImpl(template, timestampGenerator);
    }

    @Test
    public void testCreate() {
        // given
        Submission submission = Mockito.mock(Submission.class);
        // when
        repository.create(submission);
        // then
        verify(template).insert(submission);
    }

    @Test
    public void testUpdateSubmission() {
        // given
        Submission submission = Mockito.mock(Submission.class);
        // when
        repository.updateSubmission(submission);
        // then
        verify(template).save(submission);
    }

    @Test
    public void testUpdateSubmissionStatus() {
        // given
        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        when(timestampGenerator.generateTimestamp()).thenReturn(this.localDateTime);
        // when
        repository.updateSubmissionStatus(SUBMISSION_ID, status);
        // then
        verify(template).updateFirst(Query.query(Criteria.where("_id").is(SUBMISSION_ID)),
                new Update().set("status", SubmissionStatus.SUBMITTED).set("last_modified_at", localDateTime), String.class, SUBMISSIONS_COLLECTION);
    }

    @Test
    public void testFindByStatus() {
        // given
        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        int maxQueueCount = 50;
        // when
        repository.findByStatus(status, maxQueueCount);
        // then
        verify(template).find(Query.query(Criteria.where("status").is(status)).limit(maxQueueCount), Submission.class,
                SUBMISSIONS_COLLECTION);
    }

    @Test
    void testRead() {
        // given
        when(template.findById(SUBMISSION_ID, Submission.class)).thenReturn(submission);

        // when
        final Submission result = repository.read(SUBMISSION_ID);

        // then
        assertThat(result, is(submission));
    }

    @Test
    public void testReadByBarcode() {
        // given
        when(submission.getId()).thenReturn("1234");
        when(template.findOne(any(), eq(Submission.class), any())).thenReturn(submission);

        // when
        repository.readByBarcode(BARCODE);

        // then
        verify(template).findOne(Query.query(Criteria.where("form.barcode").is(BARCODE)), Submission.class,
                SUBMISSIONS_COLLECTION);
    }

    @Test
    void readByBarcodeWhenNotFound() {
        // given
        when(template.findOne(any(), eq(Submission.class), any())).thenReturn(null);

        // when
        final Submission barcode = repository.readByBarcode(BARCODE);

        // then
        assertThat(barcode, is(nullValue()));
        verify(template).findOne(Query.query(Criteria.where("form.barcode").is(BARCODE)), Submission.class,
            SUBMISSIONS_COLLECTION);
    }

    @Test
    public void testUpdateSubmissionStatusByBarcode() {
        // given
        when(timestampGenerator.generateTimestamp()).thenReturn(localDateTime);
        FesSubmissionStatus status = FesSubmissionStatus.ACCEPTED;

        // when
        repository.updateSubmissionStatusByBarcode(BARCODE, status);

        // then
        verify(template).updateFirst(Query.query(Criteria.where("form.barcode").is(BARCODE)),
                new Update().set("status", status).set("last_modified_at", localDateTime), String.class, SUBMISSIONS_COLLECTION);

    }

    @Test
    void testUpdateBarcode() {
        //given
        LocalDateTime now = LocalDateTime.now();

        //when
        repository.updateBarcode(SUBMISSION_ID, BARCODE);

        //then
        verify(template).updateFirst(Query.query(Criteria.where(ID).is(SUBMISSION_ID)),
            new Update().set(FORM_BARCODE, BARCODE).set(LAST_MODIFIED_AT, timestampGenerator.generateTimestamp()),
            String.class, SUBMISSIONS_COLLECTION);
    }

    @Test
    void testFindDelayedSubmissions() {
        //given
        LocalDateTime now = LocalDateTime.now();

        //when
        repository.findDelayedSubmissions(SubmissionStatus.PROCESSING, now);

        //then
        verify(template).find(
            Query.query(Criteria.where("status").is(SubmissionStatus.PROCESSING).and("last_modified_at").lte(now)),
            Submission.class, SUBMISSIONS_COLLECTION);
    }

    @Test
    void findSuccessfulPaidSubmissions() {
        //given
        LocalDate today = LocalDate.now();

        //when
        repository.findPaidSubmissions(PaymentReportServiceImpl.SUCCESSFUL_STATUSES, today.minusDays(1));

        //then
        verify(template).find(Query.query(
            Criteria.where(STATUS).in(PaymentReportServiceImpl.SUCCESSFUL_STATUSES).and(SUBMITTED_AT)
                .gt(today.minusDays(2)).lt(today).and(FEE_ON_SUBMISSION).exists(true)), Submission.class,
            SUBMISSIONS_COLLECTION);
    }
}