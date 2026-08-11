package uk.gov.companieshouse.efs.api.events.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3FileDeleteServiceImplTest {

    private static final String BUCKET_NAME = "file-bucket";
    private static final String SUBMISSION_ID = "submission-id";
    private static final String FILE_ID = "file-id";

    @Mock
    private S3Client s3Client;

    @Captor
    private ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestCaptor;

    private S3FileDeleteServiceImpl service;

    @BeforeEach
    void setUp() {
        this.service = new S3FileDeleteServiceImpl(s3Client, BUCKET_NAME);
    }

    @Test
    void shouldDeleteFileFromS3WhenCalledWithFileId() {
        service.deleteFile(SUBMISSION_ID, FILE_ID);

        verify(s3Client).deleteObject(deleteObjectRequestCaptor.capture());
        final var request = deleteObjectRequestCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertAll(
            () -> org.junit.jupiter.api.Assertions.assertEquals(BUCKET_NAME, request.bucket()),
            () -> org.junit.jupiter.api.Assertions.assertEquals(FILE_ID, request.key())
        );
    }

    @Test
    void shouldNotThrowWhenS3DeleteFails() {
        doThrow(SdkException.class).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertDoesNotThrow(() -> service.deleteFile(SUBMISSION_ID, FILE_ID));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
