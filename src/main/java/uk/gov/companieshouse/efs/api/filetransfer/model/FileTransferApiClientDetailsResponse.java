package uk.gov.companieshouse.efs.api.filetransfer.model;

import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * Class representing the file transfer API client response.
 */
public class FileTransferApiClientDetailsResponse {

    private String fileId;
    private HttpStatus httpStatus;
    private String fileStatus;

    public FileTransferApiClientDetailsResponse(){

    }

    public FileTransferApiClientDetailsResponse(String fileId, HttpStatus httpStatus, String fileStatus) {
        this.fileId = fileId;
        this.httpStatus = httpStatus;
        this.fileStatus = fileStatus;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FileTransferApiClientDetailsResponse that = (FileTransferApiClientDetailsResponse) o;
        return Objects.equals(getFileId(), that.getFileId()) && getHttpStatus() == that
            .getHttpStatus() && Objects.equals(getFileStatus(), that.getFileStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileId(), getHttpStatus(), getFileStatus());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileId;
        private HttpStatus httpStatus;
        private String fileStatus;

        public Builder withFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder withHttpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder withFileStatus(String fileStatus) {
            this.fileStatus = fileStatus;
            return this;
        }

        public FileTransferApiClientDetailsResponse build() {
            return new FileTransferApiClientDetailsResponse(fileId, httpStatus, fileStatus);
        }
    }

}
