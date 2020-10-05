package uk.gov.companieshouse.efs.api.events.service.exception;

public class InvalidTiffException extends RuntimeException {
    public InvalidTiffException(String message) {
        super(message);
    }

    public InvalidTiffException(String message, Throwable cause) {
        super(message, cause);
    }
}
