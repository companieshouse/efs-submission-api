package uk.gov.companieshouse.efs.api.events.service.model;

import java.util.Arrays;
import java.util.Objects;

public class FesFileModel {

    private byte[] tiffFile;
    private Integer numberOfPages;

    public FesFileModel(byte[] tiffFile, Integer numberOfPages) {
        this.tiffFile = tiffFile;
        this.numberOfPages = numberOfPages;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FesFileModel that = (FesFileModel) o;
        return Arrays.equals(getTiffFile(), that.getTiffFile()) && Objects
            .equals(getNumberOfPages(), that.getNumberOfPages());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getNumberOfPages());
        result = 31 * result + Arrays.hashCode(getTiffFile());
        return result;
    }
}
