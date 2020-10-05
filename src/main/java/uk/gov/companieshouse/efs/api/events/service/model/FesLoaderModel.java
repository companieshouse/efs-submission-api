package uk.gov.companieshouse.efs.api.events.service.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class FesLoaderModel {
    private List<FesFileModel> tiffFiles;
    private LocalDateTime barcodeDate;

    private String barcode;
    private String companyName;
    private String companyNumber;
    private String formType;

    public FesLoaderModel(String barcode, String companyName, String companyNumber, String formType,
                          List<FesFileModel> tiffFiles, LocalDateTime barcodeDate) {
        this.barcode = barcode;
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.formType = formType;
        this.tiffFiles = tiffFiles;
        this.barcodeDate = barcodeDate;
    }

    public List<FesFileModel> getTiffFiles() {
        return tiffFiles;
    }

    public void setTiffFiles(List<FesFileModel> tiffFiles) {
        this.tiffFiles = tiffFiles;
    }

    public LocalDateTime getBarcodeDate() {
        return barcodeDate;
    }

    public void setBarcodeDate(LocalDateTime barcodeDate) {
        this.barcodeDate = barcodeDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FesLoaderModel that = (FesLoaderModel) o;
        return Objects.equals(getTiffFiles(), that.getTiffFiles()) && Objects
            .equals(getBarcodeDate(), that.getBarcodeDate()) && Objects
                   .equals(getBarcode(), that.getBarcode()) && Objects
                   .equals(getCompanyName(), that.getCompanyName()) && Objects
                   .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
                   .equals(getFormType(), that.getFormType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTiffFiles(), getBarcodeDate(), getBarcode(), getCompanyName(),
            getCompanyNumber(), getFormType());
    }
}
