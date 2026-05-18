package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestClientException;
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
    private RestClient restClient;
    @Mock
    private RequestBodyUriSpec mockBodyUriSpec;
    @Mock
    private RequestBodySpec mockBodySpec;
    @Mock
    private ResponseSpec mockResponseSpec;

    @BeforeEach
    void setUp() {
        testService = new BarcodeGeneratorServiceImpl(restClient, BARCODE_GENERATOR_SERVICE_URL);
        // Setup fluent RestClient mocks
        when(restClient.post()).thenReturn(mockBodyUriSpec);
        when(mockBodyUriSpec.uri(any(String.class))).thenReturn(mockBodyUriSpec);
        when(mockBodyUriSpec.body(any(BarcodeRequest.class))).thenReturn(mockBodySpec);
        when(mockBodySpec.retrieve()).thenReturn(mockResponseSpec);
    }

    @Test
    void getEfsBarcodeSuccessfully() {
        // given
        final var barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(FIXED_BARCODE);
        when(mockResponseSpec.toEntity(BarcodeResponse.class)).thenReturn(ResponseEntity.ok(barcodeResponse));
        final var now = LocalDateTime.now();

        // when
        final var barcode = testService.getBarcode(now);

        // then
        assertThat(barcode, is(FIXED_BARCODE));
    }

    @Test
    void testRestClientExceptionHandling() {
        // given
        final var now = LocalDateTime.now();
        when(mockResponseSpec.toEntity(BarcodeResponse.class)).thenThrow(new RestClientException("oops"));

        // when
        final var exception = assertThrows(BarcodeException.class, () -> testService.getBarcode(now));

        // then
        assertEquals("Error generating barcode - message [oops]", exception.getMessage());
    }

    @Test
    void getEfsBarcodeNotPrefixedWithY() {
        // given
        final var barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(INCORRECT_BARCODE);
        when(mockResponseSpec.toEntity(BarcodeResponse.class)).thenReturn(ResponseEntity.ok(barcodeResponse));
        final var now = LocalDateTime.now();

        // when
        final var exception = assertThrows(BarcodeException.class, () -> testService.getBarcode(now));
        
        // then
        assertEquals("Error generating barcode - generated barcode [%s] not valid for EFS".formatted(INCORRECT_BARCODE),
            exception.getMessage());
    }

    @Test
    void testBarcodeResponseBodyIsNull() {
        // given
        when(mockResponseSpec.toEntity(BarcodeResponse.class)).thenReturn(ResponseEntity.ok(null));
        final var now = LocalDateTime.now();

        // when
        final var exception = assertThrows(BarcodeException.class, () -> testService.getBarcode(now));

        // then
        assertThat(exception.getMessage(), is("No content in response from Barcode service"));
    }

    @Test
    void testBarcodeResponseBodyBarcodeIsNull() {
        // given
        final var barcodeResponse = new BarcodeResponse();
        barcodeResponse.setBarcode(null);
        when(mockResponseSpec.toEntity(BarcodeResponse.class)).thenReturn(ResponseEntity.ok(barcodeResponse));
        final var now = LocalDateTime.now();

        // when
        final var exception = assertThrows(BarcodeException.class, () -> testService.getBarcode(now));

        // then
        assertThat(exception.getMessage(), is("No content in response from Barcode service"));
    }
}
