package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeRequest;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeResponse;

/**
 * Service implementation for generating barcodes using an external service.
 * <p>
 * This class uses Spring's RestClient for HTTP operations. All errors are surfaced as RestClientException
 * and must be handled by the caller. BarcodeException is thrown for business logic or HTTP errors.
 * </p>
 */
@Service
public class BarcodeGeneratorServiceImpl implements BarcodeGeneratorService {

    private final RestClient restClient;

    private final String barcodeGeneratorServiceUrl;

    public BarcodeGeneratorServiceImpl(final RestClient restClient,
        @Qualifier("barcodeGeneratorServiceUrl") final String barcodeGeneratorServiceUrl) {
        this.restClient = restClient;
        this.barcodeGeneratorServiceUrl = barcodeGeneratorServiceUrl;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Makes a POST request to the barcode generator service using RestClient. Throws BarcodeException for any error or
     * invalid response.
     *
     * @throws org.springframework.web.client.RestClientException if an HTTP error occurs
     * @throws BarcodeException                                   if the barcode is invalid or missing
     */
    @Override
    public String getBarcode(LocalDateTime date) {
        final var request = new BarcodeRequest();
        final var dateReceivedConversion = date.format(DateTimeFormatter.BASIC_ISO_DATE);

        request.setEfsBarcode(true);
        request.setDateReceived(Integer.parseInt(dateReceivedConversion));
        try {
            final var response = restClient.post()
                    .uri(barcodeGeneratorServiceUrl)
                    .body(request)
                    .retrieve()
                    .toEntity(BarcodeResponse.class);
            final var barcode = Optional.of(response)
                    .map(HttpEntity::getBody)
                    .map(BarcodeResponse::getBarcode)
                    .orElseThrow(() -> new BarcodeException("No content in response from Barcode service"));
            if (!barcode.startsWith("Y")) {
                throw new BarcodeException(
                    "Error generating barcode - generated barcode [%s] not valid for EFS".formatted(barcode));
            }
            return barcode;
        } catch (final RestClientException ex) {
            throw new BarcodeException("Error generating barcode - message [%s]".formatted(ex.getMessage()), ex);
        }

    }
}