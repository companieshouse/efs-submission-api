package uk.gov.companieshouse.efs.api.filetransfer;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;

@ExtendWith(MockitoExtension.class)
class ApiClientUtilTest {

    private ApiClientUtil apiClientUtil;

    @Mock
    private InternalFileTransferClient internalFileTransferClient;

    @BeforeEach
    void setUp() {
        apiClientUtil = new ApiClientUtil(internalFileTransferClient);
    }

    @Test
    void getInternalFileTransferClientSetsBasePathAndReturnsClient() {
        final var fileTransferApiUrl = "http://file-transfer-api";

        final var client = apiClientUtil.getInternalFileTransferClient(fileTransferApiUrl);

        verify(internalFileTransferClient).setBasePath(fileTransferApiUrl);
        assertThat(client, sameInstance(internalFileTransferClient));
    }
}

