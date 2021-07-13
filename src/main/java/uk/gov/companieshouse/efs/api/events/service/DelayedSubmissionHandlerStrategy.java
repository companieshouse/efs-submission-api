package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

public interface DelayedSubmissionHandlerStrategy {
    DelayedSubmissionHandlerContext.ServiceLevel getServiceLevel();
    
    List<Submission> findDelayedSubmissions(LocalDateTime handledAt);

    void buildAndSendEmails(List<Submission> submissions, LocalDateTime handledAt);
}
