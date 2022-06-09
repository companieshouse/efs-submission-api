package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeRequest;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeResponse;

@Service
public class BarcodeGeneratorServiceImpl implements BarcodeGeneratorService {

    private RestTemplate template;

    private String barcodeGeneratorServiceUrl;

    @Autowired
    public BarcodeGeneratorServiceImpl(RestTemplate template,
        @Qualifier("barcodeGeneratorServiceUrl") String barcodeGeneratorServiceUrl) {
        this.template = template;
        this.barcodeGeneratorServiceUrl = barcodeGeneratorServiceUrl;
    }

    @Override
    public String getBarcode(LocalDateTime date) {

        BarcodeRequest request = new BarcodeRequest();

        String dateReceivedConversion = date.format(DateTimeFormatter.BASIC_ISO_DATE);

        request.setEfsBarcode(true);
        request.setDateReceived(Integer.parseInt(dateReceivedConversion));

        try {
            String barcode = Optional.ofNullable(
                            template.postForEntity(barcodeGeneratorServiceUrl, request, BarcodeResponse.class))
                    .map(HttpEntity::getBody)
                    .map(BarcodeResponse::getBarcode)
                    .orElseThrow(() -> new BarcodeException("No content in response from Barcode service"));
            if (!barcode.startsWith("Y")) {
                throw new BarcodeException(
                        String.format("Error generating barcode - generated barcode [%s] not valid for EFS", barcode));
            }
            return barcode;
        } catch (RestClientException ex) {
            throw new BarcodeException(String.format("Error generating barcode - message [%s]", ex.getMessage()), ex);
        }

    }
}