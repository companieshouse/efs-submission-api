package uk.gov.companieshouse.efs.api.events.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import uk.gov.companieshouse.efs.api.events.service.exception.TiffDownloadException;

@ExtendWith(MockitoExtension.class)
public class TiffDownloadServiceImplTest {

    private TiffDownloadService service;

    @Mock
    private S3Client s3Client;

    private String bucket = "test-bucket";

    @Mock
    private GetObjectResponse resp;

    @Mock
    private ResponseInputStream<GetObjectResponse> responseInputStream;

    @BeforeEach
    public void setUp() {
        service = new TiffDownloadServiceImpl(s3Client, bucket);
    }

    @Test
    public void downloadTiffFileSuccessTest() {
        //given
        String fileId = "87878787";

        ByteArrayInputStream in = new ByteArrayInputStream("Hello".getBytes());
        AbortableInputStream ais = AbortableInputStream.create(in);
        when(s3Client.getObject((GetObjectRequest) any()))
                .thenReturn(new ResponseInputStream<GetObjectResponse>(resp, ais));

        // when
        byte[] actual = service.downloadTiffFile(fileId);

        // then
        assertArrayEquals("Hello".getBytes(), actual);
        verify(s3Client).getObject(GetObjectRequest.builder().bucket(bucket)
                .key(String.format("%s/%s", "converted-tiffs", fileId)).build());
    }

    @Test
    public void downloadTiffFileThrowAwsExceptionTest(){
        //given
        String fileId = "87878787";
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        //when
        Executable actual = () -> service.downloadTiffFile(fileId);

        //verify
        TiffDownloadException exception = assertThrows(TiffDownloadException.class, actual);
        assertEquals("Failed to download TIFF", exception.getMessage());
    }

    @Test
    public void testTiffDownloadServiceThrowsTiffDownloadExceptionIfIOExceptionThrown() throws IOException {
        //given
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        when(responseInputStream.read(any())).thenThrow(IOException.class);

        //when
        Executable actual = () -> service.downloadTiffFile("87878787");

        //then
        TiffDownloadException exception = assertThrows(TiffDownloadException.class, actual);
        assertEquals("Failed to convert TIFF to byteArray", exception.getMessage());
    }

}
