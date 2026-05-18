package uk.gov.companieshouse.efs.api.client;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

public class EfsRestClient {
    private final RestClient restClient;

    public EfsRestClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    public byte[] getSchema(final String schemaRegistryUrl, final String emailSchemaUri) {
        final var schemaUrl = "%s%s".formatted(schemaRegistryUrl, emailSchemaUri);
        final var headers = new HttpHeaders();

        return restClient.get()
                .uri(schemaUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .body(byte[].class);
    }
}
