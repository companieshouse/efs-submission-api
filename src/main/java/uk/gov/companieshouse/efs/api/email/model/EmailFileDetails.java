package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;

public class EmailFileDetails {

    private FileDetails fileDetails;
    private String fileLink;

    public EmailFileDetails(FileDetails fileDetails, String fileLink) {
        this.fileDetails = fileDetails;
        this.fileLink = fileLink;
    }

    public EmailFileDetails() {
    }

    public FileDetails getFileDetails() {
        return fileDetails;
    }

    public void setFileDetails(final FileDetails fileDetails) {
        this.fileDetails = fileDetails;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(final String fileLink) {
        this.fileLink = fileLink;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmailFileDetails that = (EmailFileDetails) o;
        return getFileDetails().equals(that.getFileDetails()) && Objects
            .equals(getFileLink(), that.getFileLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileDetails(), getFileLink());
    }
}
