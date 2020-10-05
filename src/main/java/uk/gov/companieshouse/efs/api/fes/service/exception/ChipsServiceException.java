package uk.gov.companieshouse.efs.api.fes.service.exception;

public class ChipsServiceException  extends RuntimeException {

    public ChipsServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChipsServiceException(String message) {
        super(message);
    }

}
