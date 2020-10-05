package uk.gov.companieshouse.efs.api.filetransfer.model;

import java.util.Objects;

public class FileTransferLinks {

    private String download;
    private String self;

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FileTransferLinks that = (FileTransferLinks) o;
        return Objects.equals(getDownload(), that.getDownload()) && Objects
            .equals(getSelf(), that.getSelf());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDownload(), getSelf());
    }
}