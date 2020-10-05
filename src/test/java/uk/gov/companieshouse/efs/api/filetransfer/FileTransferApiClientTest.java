package uk.gov.companieshouse.efs.api.filetransfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferDetails;

@ExtendWith(MockitoExtension.class)
public class FileTransferApiClientTest {

    private FileTransferApiClient fileTransferApiClient;

    @Mock
    private FileTransferApiResponseHandler responseHandler;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<FileTransferDetails> response;

    @Mock
    private FileTransferApiClientDetailsResponse mappedResponse;

    @BeforeEach
    void setUp() {
        fileTransferApiClient = new FileTransferApiClient(restTemplate, responseHandler, "/path/to/api", "F00DFACE");
    }

    @Test
    void testFileTransferApiClientReturnsAvStatusIfResponseIs200OK() {
        //given
        when(restTemplate.exchange(anyString(), any(), Mockito.<HttpEntity<Void>>any(), eq(FileTransferDetails.class))).thenReturn(response);
        when(responseHandler.handleResponse(any())).thenReturn(mappedResponse);
        when(mappedResponse.getFileStatus()).thenReturn("clean");
        when(mappedResponse.getHttpStatus()).thenReturn(HttpStatus.OK);

        //when
        FileTransferApiClientDetailsResponse actual = fileTransferApiClient.details("abc123");

        //then
        assertEquals(HttpStatus.OK, actual.getHttpStatus());
        assertEquals("clean", actual.getFileStatus());
        verify(restTemplate).exchange(eq("/path/to/api/abc123"), eq(HttpMethod.GET), Mockito.<HttpEntity<Void>>any(), eq(FileTransferDetails.class));
        verify(responseHandler).handleResponse(response);
    }

    @Test
    void testFileTransferApiClientReturnsInternalServerError(){
        //given
        when(restTemplate.exchange(anyString(), any(), Mockito.<HttpEntity<Void>>any(), eq(FileTransferDetails.class))).thenThrow(RestClientException.class);

        //when
        FileTransferApiClientDetailsResponse actual = fileTransferApiClient.details("abc123");

        //then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getHttpStatus());
        assertNull(actual.getFileStatus());
    }

    @Test
    void testFileTransferApiClientReturnsNotFound(){
        //given
        when(restTemplate.exchange(anyString(), any(), Mockito.<HttpEntity<Void>>any(), eq(FileTransferDetails.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        //when
        FileTransferApiClientDetailsResponse actual = fileTransferApiClient.details("abc123");

        //then
        assertEquals(HttpStatus.NOT_FOUND, actual.getHttpStatus());
        assertNull(actual.getFileStatus());
    }

}
