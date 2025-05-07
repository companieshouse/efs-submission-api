package uk.gov.companieshouse.efs.api.client.exception;

public class EmailClientException extends RuntimeException {

    public EmailClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
