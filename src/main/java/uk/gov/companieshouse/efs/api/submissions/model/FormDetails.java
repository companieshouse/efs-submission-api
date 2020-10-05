package uk.gov.companieshouse.efs.api.submissions.model;

import java.util.List;
import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Field;

public class FormDetails {

    private String barcode;

    @Field("form_type")
    private String formType;

    @Field("file_details")
    private List<FileDetails> fileDetailsList;

    public FormDetails(String barcode, String formType, List<FileDetails> fileDetailsList) {
        super();
        this.barcode = barcode;
        this.formType = formType;
        this.fileDetailsList = fileDetailsList;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public List<FileDetails> getFileDetailsList() {
        return fileDetailsList;
    }

    public void setFileDetailsList(List<FileDetails> fileDetailsList) {
        this.fileDetailsList = fileDetailsList;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode, fileDetailsList, formType);
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
        FormDetails other = (FormDetails) obj;
        return Objects.equals(barcode, other.barcode) && Objects.equals(fileDetailsList, other.fileDetailsList)
                && Objects.equals(formType, other.formType);
    }

    public static class Builder {
        private String barcode;
        private String formType;
        private List<FileDetails> fileDetailsList;

        public Builder withBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public Builder withFormType(String formType) {
            this.formType = formType;
            return this;
        }

        public Builder withFileDetailsList(List<FileDetails> fileDetailsList) {
            this.fileDetailsList = fileDetailsList;
            return this;
        }

        public FormDetails build() {
            return new FormDetails(barcode, formType, fileDetailsList);
        }
    }
}
