package uk.gov.companieshouse.efs.api.events.service;

import java.util.List;
import uk.gov.companieshouse.api.model.efs.events.FileConversionStatusApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public interface EventService {

    List<Submission> findSubmissionsByStatus(SubmissionStatus status);

    void processFiles();

    void submitToFes();

    void updateConversionFileStatus(String submissionId, String fileId, FileConversionStatusApi fileConversionStatusApi);

    void handleDelayedSubmissions(final DelayedSubmissionHandlerContext.ServiceLevel serviceLevel);
}
