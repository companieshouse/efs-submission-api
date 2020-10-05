package uk.gov.companieshouse.efs.api.submissions.service.exception;

public class SubmissionIncorrectStateException extends RuntimeException {

    public SubmissionIncorrectStateException(String message) {
        super(message);
    }

}
