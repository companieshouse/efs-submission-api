package uk.gov.companieshouse.efs.api.email.exception;

public class EmailServiceException extends RuntimeException {

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailServiceException(String message) {
        super(message);
    }

}
