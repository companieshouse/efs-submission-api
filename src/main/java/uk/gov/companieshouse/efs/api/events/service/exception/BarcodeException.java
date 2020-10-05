package uk.gov.companieshouse.efs.api.events.service.exception;

public class BarcodeException extends RuntimeException {
    public BarcodeException(String message) {
        super(message);
    }

    public BarcodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
