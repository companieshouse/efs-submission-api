package uk.gov.companieshouse.efs.api.events.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BarcodeRequest {

    @JsonProperty("datereceived")
    private int dateReceived;

    @JsonProperty("efsbarcode")
    private boolean efsBarcode;

    public BarcodeRequest() {
    }

    public BarcodeRequest(int dateReceived, boolean efsBarcode) {
        this.dateReceived = dateReceived;
        this.efsBarcode = efsBarcode;
    }

    public int getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(final int dateReceived) {
        this.dateReceived = dateReceived;
    }

    public boolean isEfsBarcode() {
        return efsBarcode;
    }

    public void setEfsBarcode(final boolean efsBarcode) {
        this.efsBarcode = efsBarcode;
    }
}