package uk.gov.companieshouse.efs.api.filetransfer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferDetails;

@Component
public class FileTransferApiResponseHandler {

    public FileTransferApiClientDetailsResponse handleResponse(ResponseEntity<FileTransferDetails> responseEntity) {
        FileTransferApiClientDetailsResponse fileTransferApiClientDetailsResponse = new FileTransferApiClientDetailsResponse();
        if (responseEntity != null) {
            fileTransferApiClientDetailsResponse.setHttpStatus(responseEntity.getStatusCode());
            fileTransferApiClientDetailsResponse.setFileStatus(responseEntity.getBody().getAvStatus());
        } else {
            fileTransferApiClientDetailsResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileTransferApiClientDetailsResponse;
    }
}
