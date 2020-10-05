package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;

public interface BarcodeGeneratorService {

    String getBarcode(LocalDateTime date);

}
