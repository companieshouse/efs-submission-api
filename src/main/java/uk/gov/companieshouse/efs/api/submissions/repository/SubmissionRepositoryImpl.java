package uk.gov.companieshouse.efs.api.submissions.repository;

import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.ACCEPTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSED_BY_EMAIL;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.PROCESSING;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.READY_TO_SUBMIT;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.REJECTED;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SENT_TO_FES;
import static uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus.SUBMITTED;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Sort;
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
    private static final String LAST_MODIFIED_AT = "last_modified_at";
    private static final String CREATED_AT = "created_at";
    private static final String SUBMITTED_AT = "submitted_at";
    private static final String FEE_ON_SUBMISSION = "fee_on_submission";
    private static final Pattern SAMEDAY_FORM_PATTERN = Pattern.compile("SAMEDAY$");
    private MongoTemplate template;
    private CurrentTimestampGenerator timestampGenerator;
    private static final String SUBMISSIONS_COLLECTION = "submissions";
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    public static final ImmutableSet<SubmissionStatus> SUCCESSFUL_STATUSES =
        Sets.immutableEnumSet(SUBMITTED, PROCESSING, PROCESSED_BY_EMAIL, READY_TO_SUBMIT, ACCEPTED, REJECTED,
            SENT_TO_FES);

    public SubmissionRepositoryImpl(MongoTemplate mongoTemplate, CurrentTimestampGenerator timestampGenerator) {
        this.template = mongoTemplate;
        this.timestampGenerator = timestampGenerator;
    }

    /**
     * Find submissions by status, with 'SAMEDAY' forms prioritised.<br/>
     * Implemented as two queries for convenience to avoid complex aggregation operations.<br/>
     * A better solution would involve adding a { same_day: boolean } document field from computing
     * {@code $gt: [ { $indexOfCP: [ '$form.form_type', 'SAMEDAY' ] }, 0] }<br/>
     * i.e. boolean of form.form_type field contains substring 'SAMEDAY', then sorting by same_day descending
     * @param status submission status to match
     * @param maxBatchSize max. number of entities to return in all
     * @return collection of matching entities ordered by ('SAMEDAY' DESC, created/submitted timestamp ASC)
     */
    @Override
    public List<Submission> findByStatusOrderByPriority(final SubmissionStatus status, final int maxBatchSize) {
        LOGGER.debug(String.format("Fetching submissions with status: [%s]", status));
        final Sort createdOrder = Sort.by(Sort.Direction.ASC, CREATED_AT);
        List<Submission> samedayList = template.find(
                Query.query(Criteria.where(STATUS).is(status).and(FORM_TYPE).regex(SAMEDAY_FORM_PATTERN))
                        .with(createdOrder).limit(maxBatchSize), Submission.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Found [%d] SAMEDAY submissions with status: [%s]", samedayList.size(), status));
        List<Submission> nonSamedayList = template.find(
                Query.query(Criteria.where(STATUS).is(status).and(FORM_TYPE).not().regex(SAMEDAY_FORM_PATTERN))
                        .with(createdOrder).limit(maxBatchSize - samedayList.size()), Submission.class,
                SUBMISSIONS_COLLECTION);
        LOGGER.debug(
                String.format("Found [%d] non-SAMEDAY submissions with status: [%s]", nonSamedayList.size(), status));
        final List<Submission> priorityList =
                Stream.concat(samedayList.stream(), nonSamedayList.stream()).collect(Collectors.toList());
        LOGGER.debug(String.format("Found in all [%d] submissions with status: [%s]", priorityList.size(), status));

        return priorityList;
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
        final LocalDateTime lastModified = timestampGenerator.generateTimestamp();

        template.updateFirst(Query.query(Criteria.where(ID).is(id)),
            new Update().set(STATUS, submissionStatus).set(LAST_MODIFIED_AT, lastModified),
            String.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Updated submission status [%s] at [%s]", submissionStatus,
            DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
    }

    @Override
    public void updateSubmission(Submission submission) {
        template.save(submission);
    }

    @Override
    public void updateSubmissionStatusByBarcode(String barcode, FesSubmissionStatus fesSubmissionStatus) {
        LOGGER.debug(
            String.format("Updating submission status [%s] for barcode [%s]", fesSubmissionStatus,
                barcode));
        final LocalDateTime lastModified = timestampGenerator.generateTimestamp();
        template.updateFirst(Query.query(Criteria.where(BARCODE).is(barcode)),
            new Update().set(STATUS, fesSubmissionStatus).set(LAST_MODIFIED_AT, lastModified),
            String.class, SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Updated submission status [%s] for barcode [%s] at [%s]",
            fesSubmissionStatus, barcode,
            DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
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
        final LocalDateTime lastModified = timestampGenerator.generateTimestamp();
        template.updateFirst(Query.query(Criteria.where(ID).is(id)),
            new Update().set(BARCODE, barcode).set(LAST_MODIFIED_AT, lastModified), String.class,
            SUBMISSIONS_COLLECTION);
        LOGGER.debug(String.format("Updated submission barcode to [%s] at [%s]", barcode,
            DateTimeFormatter.ISO_INSTANT.format(lastModified.atZone(ZoneId.of("UTC")))));
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
            .and(FORM_TYPE)
            .ne("SH19_SAMEDAY")), Submission.class, SUBMISSIONS_COLLECTION);
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
            .is("SH19_SAMEDAY")), Submission.class, SUBMISSIONS_COLLECTION);
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
                submissions.size(), statuses, startDate, endDate));
        return submissions;
    }

}
