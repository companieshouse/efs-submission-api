package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.efs.api.events.service.exception.BarcodeException;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeRequest;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeResponse;

@ExtendWith(MockitoExtension.class)
class BarcodeGeneratorServiceImplTest {

    private static final String BARCODE_GENERATOR_SERVICE_URL = "http://localhost:4422/";
    private static final String FIXED_BARCODE = "Y002HWHC";
    private static final String INCORRECT_BARCODE = "X002HWHC";

    private BarcodeGeneratorService testService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        testService = new BarcodeGeneratorServiceImpl(restTemplate, BARCODE_GENERATOR_SERVICE_URL);
    }

    @Test
    void getEfsBarcodeSuccessfully() {
        //given
        BarcodeResponse barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(FIXED_BARCODE);
        ResponseEntity<BarcodeResponse> response = ResponseEntity.ok(barcodeResponse);
        when(restTemplate.postForEntity(eq(BARCODE_GENERATOR_SERVICE_URL), isA(BarcodeRequest.class),
                eq(BarcodeResponse.class))).thenReturn(response);
        LocalDateTime now = LocalDateTime.now();

        //when
        String barcode = testService.getBarcode(now);

        //then
        assertThat(barcode, is(FIXED_BARCODE));
    }


    @Test
    void testRestClientExceptionHandling() {

        //given
        LocalDateTime now = LocalDateTime.now();
        BarcodeResponse barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(FIXED_BARCODE);
        when(restTemplate.postForEntity(eq(BARCODE_GENERATOR_SERVICE_URL), isA(BarcodeRequest.class),
                eq(BarcodeResponse.class))).thenThrow(new RestClientException("oops"));

        //when
        Executable actual = () -> testService.getBarcode(now);

        //then
        BarcodeException exception = assertThrows(BarcodeException.class, actual);
        assertEquals("Error generating barcode - message [oops]", exception.getMessage());

    }

    @Test
    void getEfsBarcodeNotPrefixedWithY() {
        //given
        BarcodeResponse barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(INCORRECT_BARCODE);
        ResponseEntity<BarcodeResponse> response = ResponseEntity.ok(barcodeResponse);
        when(restTemplate.postForEntity(eq(BARCODE_GENERATOR_SERVICE_URL), isA(BarcodeRequest.class),
                eq(BarcodeResponse.class))).thenReturn(response);
        LocalDateTime now = LocalDateTime.now();

        //when
        Executable actual = () -> testService.getBarcode(now);

        //then
        BarcodeException exception = assertThrows(BarcodeException.class, actual);
        assertEquals("Error generating barcode - generated barcode ["
                + INCORRECT_BARCODE + "] not valid for EFS", exception.getMessage());
    }

}
