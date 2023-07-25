package uk.gov.companieshouse.efs.api.events.service.model;

import java.util.Arrays;
import java.util.Objects;

public class FesFileModel {

    private byte[] tiffFile;
    private Integer numberOfPages;
    private String attachmentType;

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public FesFileModel(byte[] tiffFile, Integer numberOfPages) {
        this.tiffFile = tiffFile;
        this.numberOfPages = numberOfPages;
    }

    public FesFileModel(byte[] tiffFile, Integer numberOfPages, String attachmentType){
       this(tiffFile, numberOfPages);
       this.attachmentType = attachmentType;
    }

    public byte[] getTiffFile() {
        return tiffFile;
    }

    public void setTiffFile(byte[] tiffFile) {
        this.tiffFile = tiffFile;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FesFileModel that = (FesFileModel) o;
        return Arrays.equals(tiffFile, that.tiffFile) && Objects.equals(numberOfPages, that.numberOfPages) && Objects.equals(attachmentType, that.attachmentType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(numberOfPages, attachmentType);
        result = 31 * result + Arrays.hashCode(tiffFile);
        return result;
    }
}
