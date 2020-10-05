package uk.gov.companieshouse.efs.api.submissions.service.exception;

public class SubmissionNotFoundException extends RuntimeException {

    public SubmissionNotFoundException(String message) {
        super(message);
    }

}
