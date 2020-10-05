package uk.gov.companieshouse.efs.api.filetransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class FileTransferDetails {

    @JsonProperty("av_status")
    private String avStatus;
    @JsonProperty("av_timestamp")
    private String avTimestamp;
    @JsonProperty("content_type")
    private String contentType;
    private String id;
    private FileTransferLinks links;
    private String name;
    private Long size;

    public String getAvStatus() {
        return avStatus;
    }

    public void setAvStatus(String avStatus) {
        this.avStatus = avStatus;
    }

    public String getAvTimestamp() {
        return avTimestamp;
    }

    public void setAvTimestamp(String avTimestamp) {
        this.avTimestamp = avTimestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FileTransferLinks getLinks() {
        return links;
    }

    public void setLinks(FileTransferLinks links) {
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FileTransferDetails that = (FileTransferDetails) o;
        return Objects.equals(getAvStatus(), that.getAvStatus())
               && Objects.equals(getAvTimestamp(), that.getAvTimestamp())
               && Objects.equals(getContentType(), that.getContentType())
               && Objects.equals(getId(), that.getId())
               && Objects.equals(getLinks(), that.getLinks())
               && Objects.equals(getName(), that.getName())
               && Objects.equals(getSize(), that.getSize());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getAvStatus(), getAvTimestamp(), getContentType(), getId(), getLinks(), getName(),
                getSize());
    }
}