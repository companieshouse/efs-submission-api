package uk.gov.companieshouse.efs.api.events.service;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Deletes uploaded submission files from the configured S3 file bucket.
 *
 * <p>Delete failures are logged with submission context and are not rethrown, so
 * callers can continue processing other files.</p>
 */
@Component
public class S3FileDeleteServiceImpl implements S3FileDeleteService {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private final S3Client s3;
    private final String bucketName;

    /**
     * Creates a delete service for the configured file upload bucket.
     *
     * @param s3 AWS S3 client used to issue delete requests
     * @param bucketName bucket containing uploaded submission files
     */
    public S3FileDeleteServiceImpl(final S3Client s3, @Value("${file.bucket.name}") final String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    /**
     * Deletes a single file object from S3 by file ID (object key).
     *
     * <p>If S3 deletion fails, the error is logged with the submission ID and file metadata,
     * and processing continues without throwing.</p>
     *
     * @param submissionId submission identifier used for contextual logging
     * @param fileId S3 object key for the uploaded file
     */
    @Override
    public void deleteFile(final String submissionId, final String fileId) {
        final var debugMap = buildLogMap(fileId);
        LOGGER.debugContext(submissionId, "Deleting file from S3", debugMap);
        try {
            final var request = DeleteObjectRequest.builder()
                                                   .bucket(bucketName)
                                                   .key(fileId)
                                                   .build();
            s3.deleteObject(request);
        } catch (SdkException ex) {
            final var errorMap = buildLogMap(fileId);
            LOGGER.errorContext(submissionId, "Unable to delete file from S3", ex, errorMap);
        }
    }

    /**
     * Builds structured log data for S3 delete operations.
     *
     * @param fileId S3 object key for the file being deleted
     * @return log map containing file and bucket context
     */
    private @NonNull Map<String, Object> buildLogMap(final String fileId) {
        final Map<String, Object> debug = new HashMap<>();
        debug.put("fileId", fileId);
        debug.put("bucketName", bucketName);
        return debug;
    }
}
