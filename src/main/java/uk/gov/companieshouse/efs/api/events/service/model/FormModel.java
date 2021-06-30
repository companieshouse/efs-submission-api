package uk.gov.companieshouse.efs.api.events.service.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
    private boolean sameDayService;

    private FormModel() {
        // no direct instantiation
    }

    public Long getEnvelopeId() {
        return envelopeId;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getFormType() {
        return formType;
    }

    public Long getFormId() {
        return formId;
    }

    public Long getImageId() {
        return imageId;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public Long getFormStatus() {
        return formStatus;
    }

    public LocalDateTime getBarcodeDate() {
        return barcodeDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSameDayIndicator() {
        return sameDayService ? "Y" : "N";
    }

    public static class Builder {
        private List<Consumer<FormModel>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }
        
        public Builder withEnvelopeId(Long envelopeId) {
            buildSteps.add(data -> data.envelopeId = envelopeId);
            return this;
        }

        public Builder withBarcode(String barcode) {
            buildSteps.add(data -> data.barcode = barcode);
            return this;
        }

        public Builder withCompanyName(String companyName) {
            buildSteps.add(data -> data.companyName = companyName);
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            buildSteps.add(data -> data.companyNumber = companyNumber);
            return this;
        }

        public Builder withFormType(String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }

        public Builder withFormId(Long formId) {
            buildSteps.add(data -> data.formId = formId);
            return this;
        }

        public Builder withImageId(Long imageId) {
            buildSteps.add(data -> data.imageId = imageId);
            return this;
        }

        public Builder withNumberOfPages(Integer numberOfPages) {
            buildSteps.add(data -> data.numberOfPages = numberOfPages);
            return this;
        }

        public Builder withFormStatus(Long formStatus) {
            buildSteps.add(data -> data.formStatus = formStatus);
            return this;
        }

        public Builder withBarcodeDate(LocalDateTime barcodeDate) {
            buildSteps.add(data -> data.barcodeDate = barcodeDate);
            return this;
        }
        
        public Builder withSameDayService(boolean sameDayService) {
            buildSteps.add(data -> data.sameDayService = sameDayService);
            return this;
        }

        public FormModel build() {
            final FormModel data = new FormModel();
            
            buildSteps.forEach(step -> step.accept(data));
            
            return data;
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
        return sameDayService == formModel.sameDayService && Objects.equals(getEnvelopeId(),
            formModel.getEnvelopeId()) && Objects.equals(getBarcode(), formModel.getBarcode())
            && Objects.equals(getCompanyName(), formModel.getCompanyName()) && Objects.equals(
            getCompanyNumber(), formModel.getCompanyNumber()) && Objects.equals(getFormType(),
            formModel.getFormType()) && Objects.equals(getFormId(), formModel.getFormId())
            && Objects.equals(getImageId(), formModel.getImageId()) && Objects.equals(
            getNumberOfPages(), formModel.getNumberOfPages()) && Objects.equals(getFormStatus(),
            formModel.getFormStatus()) && Objects.equals(getBarcodeDate(),
            formModel.getBarcodeDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnvelopeId(), getBarcode(), getCompanyName(), getCompanyNumber(),
            getFormType(), getFormId(), getImageId(), getNumberOfPages(), getFormStatus(),
            getBarcodeDate(), sameDayService);
    }
}