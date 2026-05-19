package uk.gov.companieshouse.efs.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * This test uses raw types and unchecked calls for mocking the fluent generic API of RestClient,
 * which is necessary due to limitations in Mockito's handling of chained generics.
 * All such warnings are intentionally suppressed.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class EfsRestClientTest {
    @Mock
    private RestClient mockRestClient;
    @Mock
    private RequestHeadersUriSpec mockUriSpec; // raw type
    @Mock
    private RequestHeadersSpec mockHeadersSpec; // raw type
    @Mock
    private ResponseSpec mockResponseSpec;
    @InjectMocks
    private EfsRestClient efsRestClient;

    @BeforeEach
    void setUp() {
        when(mockRestClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(any(String.class))).thenReturn(mockUriSpec);
        when(mockUriSpec.headers(any())).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
    }

    @Test
    void getSchema_returnsByteArray() {
        final var schemaRegistryUrl = "http://localhost:8080/";
        final var emailSchemaUri = "schema";
        final var expected = new byte[]{1, 2, 3};

        when(mockResponseSpec.body(byte[].class)).thenReturn(expected);

        final var actual = efsRestClient.getSchema(schemaRegistryUrl, emailSchemaUri);
        assertThat(actual, is(expected));
    }
}
