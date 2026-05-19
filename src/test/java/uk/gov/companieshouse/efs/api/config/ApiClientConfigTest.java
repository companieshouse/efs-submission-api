package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;

class ApiClientConfigTest {

    private ApiClientConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new ApiClientConfig();
        ReflectionTestUtils.setField(testConfig, "internalApiKey", "test-api-key");
    }

    @Test
    void getInternalFileClientBuildsClientWithConfiguredApiKey() {
        final var client = testConfig.getInternalFileClient();

        assertThat(client, isA(InternalFileTransferClient.class));
    }
}

