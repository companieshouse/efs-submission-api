package uk.gov.companieshouse.efs.api.filetransfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferApiClientDetailsResponse;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferDetails;

@ExtendWith(MockitoExtension.class)
class FileTransferApiResponseHandlerTest {
    private FileTransferApiResponseHandler responseHandler;

    @Mock
    private FileTransferDetails fileTransferDetails;

    @BeforeEach
    void setUp() {
        this.responseHandler = new FileTransferApiResponseHandler();
    }

    @Test
    void testResponseExtractorReturnsInternalServerErrorIfResponseIsNull() {
        //when
        FileTransferApiClientDetailsResponse actual = responseHandler.handleResponse(null);

        //then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getHttpStatus());
        assertNull(actual.getFileStatus());
    }

    @Test
    void testResponseExtractorReturnsMetadata() {
        //given
        when(fileTransferDetails.getAvStatus()).thenReturn("clean");
        ResponseEntity<FileTransferDetails> response = ResponseEntity.ok(fileTransferDetails);

        //when
        FileTransferApiClientDetailsResponse actual = responseHandler.handleResponse(response);

        //then
        assertEquals(HttpStatus.OK, actual.getHttpStatus());
        assertEquals("clean", actual.getFileStatus());
    }
}
