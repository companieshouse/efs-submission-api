package uk.gov.companieshouse.efs.api.submissions.service.exception;

public class FileIncorrectStateException extends RuntimeException {

    public FileIncorrectStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileIncorrectStateException(String message) {
        super(message);
    }

}
