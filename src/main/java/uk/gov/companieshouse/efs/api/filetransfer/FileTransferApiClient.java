package uk.gov.companieshouse.efs.api.filetransfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferDetails;

/**
 * Instances of this class are responsible for fetching metadata from file transfer api.
 */
@Component
public class FileTransferApiClient {

    private static final String DETAILS_URI = "%s/%s";
    private static final String HEADER_API_KEY = "x-api-key";

    private RestTemplate restTemplate;

    private FileTransferApiResponseHandler responseHandler;

    private String fileTransferApiKey;

    private String fileTransferApiUrl;

    /**
     * Constructor.
     *
     * @param restTemplate          dependency
     * @param responseHandler       dependency
     * @param fileTransferApiUrl    dependency
     * @param fileTransferApiKey    dependency
     */
    @Autowired
    public FileTransferApiClient(RestTemplate restTemplate,
                                 FileTransferApiResponseHandler responseHandler,
                                 @Value("${file.transfer.api.url}") String fileTransferApiUrl,
                                 @Value("${file.transfer.api.key}") String fileTransferApiKey) {
        this.fileTransferApiUrl = fileTransferApiUrl;
        this.responseHandler = responseHandler;
        this.fileTransferApiKey = fileTransferApiKey;
        this.restTemplate = restTemplate;
    }

    /**
     * Get metadata for the given file from file-transfer-api.
     *
     * @param fileId The id used by the file-transfer-api to identify the file
     * @return FileTransferApiClientDetailsResponse containing the http status and file status
     */
    public FileTransferApiClientDetailsResponse details(String fileId) {
        final String downloadUri = String.format(DETAILS_URI, fileTransferApiUrl, fileId);
        FileTransferApiClientDetailsResponse response = new FileTransferApiClientDetailsResponse();
        try {
            return responseHandler.handleResponse(restTemplate.exchange(downloadUri, HttpMethod.GET, new HttpEntity<>(withApiKeyHeader()), FileTransferDetails.class));
        } catch (HttpStatusCodeException hsce) {
            response.setHttpStatus(hsce.getStatusCode());
        } catch (RestClientException ex) {
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private HttpHeaders withApiKeyHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }
}