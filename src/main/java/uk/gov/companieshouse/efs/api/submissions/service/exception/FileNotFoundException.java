package uk.gov.companieshouse.efs.api.submissions.service.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(String message) {
        super(message);
    }

}
