package uk.gov.companieshouse.efs.api.filetransfer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class FileTransferServiceTest {

    private static final String FILE_TRANSFER_URL = "http://file-transfer-api";
    private static final String FILE_ID = "file-id-123";

    private FileTransferService fileTransferService;

    @Mock
    private ApiClientUtil apiClientUtil;

    @Mock
    private Logger logger;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InternalFileTransferClient internalFileTransferClient;

    @Mock
    private ApiResponse<FileDetailsApi> apiResponse;

    @Mock
    private FileDetailsApi fileDetailsApi;

    @BeforeEach
    void setUp() {
        fileTransferService = new FileTransferService(apiClientUtil, logger);
        ReflectionTestUtils.setField(fileTransferService, "fileTransferApiUrl", FILE_TRANSFER_URL);

        when(apiClientUtil.getInternalFileTransferClient(FILE_TRANSFER_URL)).thenReturn(internalFileTransferClient);
    }

    @Test
    void detailsReturnsApiResponse() throws Exception {
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute()).thenReturn(apiResponse);

        final var response = fileTransferService.details(FILE_ID);

        assertThat(response, sameInstance(apiResponse));
        verify(apiClientUtil).getInternalFileTransferClient(FILE_TRANSFER_URL);
    }

    @Test
    void getFileDetailsReturnsOptionalWithDataWhenPresent() throws Exception {
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(fileDetailsApi);

        final var result = fileTransferService.getFileDetails(FILE_ID);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), sameInstance(fileDetailsApi));
    }

    @Test
    void getFileDetailsReturnsEmptyOptionalWhenDataIsNull() throws Exception {
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(null);

        final var result = fileTransferService.getFileDetails(FILE_ID);

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void getFileDetailsReturnsEmptyOptionalForNotFound() throws Exception {
        final var apiError = org.mockito.Mockito.mock(ApiErrorResponseException.class);
        when(apiError.getStatusCode()).thenReturn(404);
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute()).thenThrow(apiError);

        final var result = fileTransferService.getFileDetails(FILE_ID);

        assertThat(result.isEmpty(), is(true));
        verify(logger, never()).errorContext(anyString(), anyString(), isNull(), anyMap());
    }

    @Test
    void getFileDetailsThrowsFileDetailsExceptionForUriValidationException() throws Exception {
        final var uriValidationException = org.mockito.Mockito.mock(URIValidationException.class);
        when(uriValidationException.getMessage()).thenReturn("invalid URI");
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute())
            .thenThrow(uriValidationException);

        final var thrown = assertThrows(FileDetailsException.class,
            () -> fileTransferService.getFileDetails(FILE_ID));

        assertThat(thrown.getMessage(), is("invalid URI"));
    }

    @Test
    void getFileDetailsLogsAndThrowsForUnexpectedApiError() throws Exception {
        final var apiError = org.mockito.Mockito.mock(ApiErrorResponseException.class);
        when(apiError.getStatusCode()).thenReturn(500);
        when(apiError.getMessage()).thenReturn("server error");
        when(internalFileTransferClient.privateFileTransferHandler().details(FILE_ID).execute()).thenThrow(apiError);

        final var thrown = assertThrows(FileDetailsException.class,
            () -> fileTransferService.getFileDetails(FILE_ID));

        assertThat(thrown.getMessage(), is("server error"));
        verify(logger).errorContext(
            eq(FILE_ID),
            eq("Unexpected response status from file transfer api when getting file details."),
            isNull(),
            eq(Map.of("expected", "200", "status", 500))
        );
    }
}

