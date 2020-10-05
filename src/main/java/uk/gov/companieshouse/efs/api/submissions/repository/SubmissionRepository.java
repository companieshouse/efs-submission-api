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

    List<Submission> findPaidSubmissions(Collection<SubmissionStatus> statuses, LocalDate submitDate);

}
