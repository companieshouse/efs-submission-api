package uk.gov.companieshouse.efs.api.submissions.repository;

import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.ACCEPTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSED_BY_EMAIL;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSING;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.READY_TO_SUBMIT;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED_BY_DOCUMENT_CONVERTER;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED_BY_VIRUS_SCAN;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SENT_TO_FES;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SUBMITTED;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Repository
public class SubmissionRepositoryImpl implements SubmissionRepository {

    private static final String ID = "_id";
    private static final String STATUS = "status";
    private static final String BARCODE = "form.barcode";
    private static final String FORM_TYPE = "form.form_type";
    private static final String SAMEDAY_REGEX = "SAMEDAY$";
    private static final String LAST_MODIFIED_AT = "last_modified_at";
    private static final String SUBMITTED_AT = "submitted_at";
    private static final String FEE_ON_SUBMISSION = "fee_on_submission";
    private MongoTemplate template;
    private CurrentTimestampGenerator timestampGenerator;
    private static final String SUBMISSIONS_COLLECTION = "submissions";
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    public static final ImmutableSet<SubmissionStatus> SUCCESSFUL_STATUSES =
        Sets.immutableEnumSet(SUBMITTED, PROCESSING, PROCESSED_BY_EMAIL, READY_TO_SUBMIT, ACCEPTED, REJECTED,
            SENT_TO_FES);

    public static final ImmutableSet<SubmissionStatus> FAILED_STATUSES =
        Sets.immutableEnumSet(REJECTED_BY_DOCUMENT_CONVERTER, REJECTED_BY_VIRUS_SCAN);
    public SubmissionRepositoryImpl(MongoTemplate mongoTemplate, CurrentTimestampGenerator timestampGenerator) {
        this.template = mongoTemplate;
        this.timestampGenerator = timestampGenerator;
    }

    @Override
    public List<Submission> findByStatus(SubmissionStatus status, int maxQueueCount) {
        LOGGER.debug(String.format("Fetching submissions with status: [%s]", status));
        List<Submission> submissions = template.find(Query.query(Criteria.where(STATUS).is(status)).limit(maxQueueCount), Submission.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Found [%d] submissions with status: [%s]", submissions.size(), status));
        return submissions;
    }

    @Override
    public Submission read(String id) {
        LOGGER.debug(String.format("Fetching submission with id: [%s] from repository", id));
        return template.findById(id, Submission.class);
    }

    @Override
    public void create(Submission submission) {
        template.insert(submission);
    }

    @Override
    public void updateSubmissionStatus(String id, SubmissionStatus submissionStatus) {
        template.updateFirst(Query.query(Criteria.where(ID).is(id)),
                new Update().set(STATUS, submissionStatus).set(LAST_MODIFIED_AT, timestampGenerator.generateTimestamp()), String.class, SUBMISSIONS_COLLECTION);

    }

    @Override
    public void updateSubmission(Submission submission) {
        template.save(submission);
    }

    @Override
    public void updateSubmissionStatusByBarcode(String barcode, FesSubmissionStatus fesSubmissionStatus) {
        LOGGER.debug(String.format("Updating submission status [%s] for barcode [%s]", fesSubmissionStatus, barcode));
        template.updateFirst(Query.query(Criteria.where(BARCODE).is(barcode)),
                new Update().set(STATUS, fesSubmissionStatus).set(LAST_MODIFIED_AT, timestampGenerator.generateTimestamp()), String.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Updated submission status [%s] for barcode [%s]", fesSubmissionStatus, barcode));
    }

    @Override
    public Submission readByBarcode(String barcode) {
        LOGGER.debug(String.format("Fetching submissions with barcode: [%s]", barcode));
        Submission submission = template.findOne(Query.query(Criteria.where(BARCODE).is(barcode)), Submission.class,
                SUBMISSIONS_COLLECTION);
        if (submission != null) {
            LOGGER.debug(String.format("Found submission [%s] with barcode: [%s]", submission.getId(), barcode));
        }
        return submission;
    }

    @Override
    public void updateBarcode(String id, String barcode) {
        template.updateFirst(Query.query(Criteria.where(ID).is(id)),
                new Update().set(BARCODE, barcode).set(LAST_MODIFIED_AT, timestampGenerator.generateTimestamp()), String.class, SUBMISSIONS_COLLECTION);
    }

    @Override
    public List<Submission> findDelayedSubmissions(SubmissionStatus status, LocalDateTime before) {
        LOGGER.debug(
            String.format("Fetching submissions with status: [%s] last modified before [%s]",
                status, before.toString()));
        List<Submission> submissions = template.find(Query.query(Criteria.where(STATUS)
            .is(status)
            .and(LAST_MODIFIED_AT)
            .lte(before)
            .not()
            .and(FORM_TYPE)
            .regex(SAMEDAY_REGEX)), Submission.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(
            String.format("Found [%d] submissions with status: [%s] last modified before [%s]",
                submissions.size(), status, before));
        return submissions;
    }

    @Override
    public List<Submission> findDelayedSameDaySubmissions(Collection<SubmissionStatus> statuses, LocalDateTime submittedBefore) {
        final String statusesString = Arrays.toString(statuses.toArray());
        LOGGER.debug(String.format("Fetching sameday submissions with statuses: %s submitted before [%s]",
            statusesString, submittedBefore));
        List<Submission> submissions = template.find(Query.query(Criteria.where(STATUS)
            .in(statuses)
            .and(SUBMITTED_AT)
            .lte(submittedBefore)
            .and(FORM_TYPE)
            .regex(SAMEDAY_REGEX)), Submission.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Found [%d] sameday submissions with statuses: %s submitted before [%s]",
            submissions.size(), statusesString, submittedBefore));
        return submissions;
    }

    @Override
    public List<Submission> findPaidSubmissions(final Collection<SubmissionStatus> statuses, final LocalDate startDate,
        final LocalDate endDate) {
        LOGGER.debug(String
            .format("Fetching paid submissions with statuses: [%s] and submitted between [%s] and [%s] (exclusive)",
                statuses, startDate.toString(), endDate.toString()));
        List<Submission> submissions = template.find(Query.query(
            Criteria.where(STATUS).in(statuses).and(SUBMITTED_AT).gte(startDate).lt(endDate).and(FEE_ON_SUBMISSION)
                .exists(true)), Submission.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String
            .format("Found [%d] paid submissions with statuses: [%s] and submitted [%s] and [%s] (exclusive)",
                submissions.size(), statuses, startDate.toString(), endDate.toString()));
        return submissions;
    }

}
