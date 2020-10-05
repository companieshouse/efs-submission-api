package uk.gov.companieshouse.efs.api.submissions.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Field;

import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;

public class FileDetails {

    @Field("file_id")
    private String fileId;

    @Field("file_name")
    private String fileName;

    @Field("file_size")
    private Long fileSize;

    @Field("converted_file_id")
    private String convertedFileId;

    @Field("conversion_status")
    private FileConversionStatus conversionStatus;

    @Field("number_of_pages")
    private Integer numberOfPages;

    @Field("last_modified_at")
    private LocalDateTime lastModifiedAt;

    public FileDetails(String fileId, String fileName, Long fileSize, String convertedFileId, FileConversionStatus conversionStatus,
            Integer numberOfPages, LocalDateTime lastModifiedAt) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.convertedFileId = convertedFileId;
        this.conversionStatus = conversionStatus;
        this.numberOfPages = numberOfPages;
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getConvertedFileId() {
        return convertedFileId;
    }

    public void setConvertedFileId(String convertedFileId) {
        this.convertedFileId = convertedFileId;
    }

    public FileConversionStatus getConversionStatus() {
        return conversionStatus;
    }

    public void setConversionStatus(FileConversionStatus conversionStatus) {
        this.conversionStatus = conversionStatus;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversionStatus, convertedFileId, fileId, fileName, fileSize, lastModifiedAt,
                numberOfPages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileDetails other = (FileDetails) obj;
        return conversionStatus == other.conversionStatus && Objects.equals(convertedFileId, other.convertedFileId)
                && Objects.equals(fileId, other.fileId) && Objects.equals(fileName, other.fileName)
                && Objects.equals(fileSize, other.fileSize) && Objects.equals(lastModifiedAt, other.lastModifiedAt)
                && Objects.equals(numberOfPages, other.numberOfPages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileId;
        private String fileName;
        private Long fileSize;
        private String convertedFileId;
        private FileConversionStatus conversionStatus;
        private Integer numberOfPages;
        private LocalDateTime lastModifiedAt;

        public Builder withFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withFileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder withConvertedFileId(String convertedFileId) {
            this.convertedFileId = convertedFileId;
            return this;
        }

        public Builder withConversionStatus(FileConversionStatus conversionStatus) {
            this.conversionStatus = conversionStatus;
            return this;
        }

        public Builder withLastModifiedAt(LocalDateTime lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
            return this;
        }

        public Builder withNumberOfPages(Integer numberOfPages) {
            this.numberOfPages = numberOfPages;
            return this;
        }

        public FileDetails build() {
            return new FileDetails(fileId, fileName, fileSize, convertedFileId, conversionStatus, numberOfPages,
                    lastModifiedAt);
        }
    }
}
