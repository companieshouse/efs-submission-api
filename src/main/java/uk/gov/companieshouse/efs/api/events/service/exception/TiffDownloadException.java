package uk.gov.companieshouse.efs.api.events.service.exception;

public class TiffDownloadException extends RuntimeException {
    public TiffDownloadException(String message) {
        super(message);
    }

    public TiffDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
