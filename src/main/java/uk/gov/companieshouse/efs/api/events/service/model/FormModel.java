package uk.gov.companieshouse.efs.api.events.service.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class FormModel {

    private Long envelopeId;
    private String barcode;
    private String companyName;
    private String companyNumber;
    private String formType;
    private Long formId;
    private Long imageId;
    private Integer numberOfPages;
    private Long formStatus;
    private LocalDateTime barcodeDate;

    public FormModel(Long envelopeId, String barcode, String companyName, String companyNumber, String formType, Long formId, Long imageId, Integer numberOfPages, Long formStatus, LocalDateTime barcodeDate) {
        this.envelopeId = envelopeId;
        this.barcode = barcode;
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.formType = formType;
        this.formId = formId;
        this.imageId = imageId;
        this.numberOfPages = numberOfPages;
        this.formStatus = formStatus;
        this.barcodeDate = barcodeDate;
    }

    public Long getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(Long envelopeId) {
        this.envelopeId = envelopeId;
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

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public Long getFormStatus() {
        return formStatus;
    }

    public void setFormStatus(Long formStatus) {
        this.formStatus = formStatus;
    }

    public LocalDateTime getBarcodeDate() {
        return barcodeDate;
    }

    public void setBarcodeDate(LocalDateTime barcodeDate) {
        this.barcodeDate = barcodeDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long envelopeId;
        private String barcode;
        private String companyName;
        private String companyNumber;
        private String formType;
        private Long formId;
        private Long imageId;
        private Integer numberOfPages;
        private Long formStatus;
        private LocalDateTime barcodeDate;

        public Builder withEnvelopeId(Long envelopeId) {
            this.envelopeId = envelopeId;
            return this;
        }

        public Builder withBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public Builder withFormId(Long formId) {
            this.formId = formId;
            return this;
        }

        public Builder withImageId(Long imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder withNumberOfPages(Integer numberOfPages) {
            this.numberOfPages = numberOfPages;
            return this;
        }

        public Builder withFormStatus(Long formStatus) {
            this.formStatus = formStatus;
            return this;
        }

        public Builder withBarcodeDate(LocalDateTime barcodeDate) {
            this.barcodeDate = barcodeDate;
            return this;
        }

        public FormModel build() {
            return new FormModel(envelopeId, barcode, companyName, companyNumber, formType, formId, imageId, numberOfPages, formStatus, barcodeDate);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FormModel formModel = (FormModel) o;
        return Objects.equals(getEnvelopeId(), formModel.getEnvelopeId()) && Objects
            .equals(getBarcode(), formModel.getBarcode()) && Objects
                   .equals(getCompanyName(), formModel.getCompanyName()) && Objects
                   .equals(getCompanyNumber(), formModel.getCompanyNumber()) && Objects
                   .equals(getFormType(), formModel.getFormType()) && Objects
                   .equals(getFormId(), formModel.getFormId()) && Objects
                   .equals(getImageId(), formModel.getImageId()) && Objects
                   .equals(getNumberOfPages(), formModel.getNumberOfPages()) && Objects
                   .equals(getFormStatus(), formModel.getFormStatus()) && Objects
                   .equals(getBarcodeDate(), formModel.getBarcodeDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnvelopeId(), getBarcode(), getCompanyName(), getCompanyNumber(),
            getFormType(), getFormId(), getImageId(), getNumberOfPages(), getFormStatus(),
            getBarcodeDate());
    }
}