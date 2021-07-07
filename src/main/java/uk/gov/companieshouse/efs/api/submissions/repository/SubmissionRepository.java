package uk.gov.companieshouse.efs.api.submissions.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import uk.gov.companieshouse.api.model.efs.fes.FesSubmissionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public interface SubmissionRepository {

    List<Submission> findByStatus(SubmissionStatus status, int maxQueueCount);

    Submission read(String id);

    void create(Submission submission);

    void updateSubmissionStatus(String id, SubmissionStatus submissionStatus);

    void updateSubmission(Submission submission);

    void updateSubmissionStatusByBarcode(String barcode, FesSubmissionStatus fesSubmissionStatus);

    Submission readByBarcode(String barcode);

    void updateBarcode(String id, String barcode);

    List<Submission> findDelayedSubmissions(SubmissionStatus status, LocalDateTime before);

    List<Submission> findDelayedSameDaySubmissions(Collection<SubmissionStatus> statuses, LocalDateTime submittedBefore);

    /**
     * Find paid submissions having any of the given statuses and submit date between startDate and endDate.
     *
     * @param statuses  the statuses to include
     * @param startDate the report period start (inclusive)
     * @param endDate   the report period end (exclusive)
     * @return the collection of submissions (may be empty)
     */
    List<Submission> findPaidSubmissions(Collection<SubmissionStatus> statuses, LocalDate startDate,
        final LocalDate endDate);

}
