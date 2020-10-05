package uk.gov.companieshouse.efs.api.events.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BarcodeResponse {

    @JsonProperty("barcode")
    private String barcode;

    public BarcodeResponse() {
    }

    public BarcodeResponse(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(final String barcode) {
        this.barcode = barcode;
    }
}