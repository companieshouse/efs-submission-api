package uk.gov.companieshouse.efs.api.filetransfer;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@Service
public class FileTransferService {

    @Value( "${file.transfer.api.url}" )
    private String fileTransferApiUrl;
    private final ApiClientUtil apiClientUtil;

    private final Logger logger;

    public FileTransferService(final ApiClientUtil apiClientUtil, final Logger logger) {
        this.apiClientUtil = apiClientUtil;
        this.logger = logger;
    }

    public Optional<FileDetailsApi> getFileDetails(final String id) {
        try {
            final var response = details(id);
            return Optional.ofNullable(response.getData());
        } catch ( final URIValidationException e) {
            throw new FileDetailsException(e.getMessage());
        } catch (final ApiErrorResponseException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            }
            final var message = "Unexpected response status from file transfer api when getting file details.";
            logger.errorContext(id, message, null, Map.of(
                    "expected", "200",
                    "status", e.getStatusCode()
            ));
            throw new FileDetailsException(e.getMessage());
        }
    }

    public ApiResponse<FileDetailsApi> details( final String fileId ) throws ApiErrorResponseException, URIValidationException {
        return apiClientUtil.getInternalFileTransferClient(fileTransferApiUrl)
                .privateFileTransferHandler()
                .details( fileId )
                .execute();
    }
}
