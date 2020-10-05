package uk.gov.companieshouse.efs.api.events.service.exception;

public class FesLoaderException extends RuntimeException {
    public FesLoaderException(String message) {
        super(message);
    }

    public FesLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
