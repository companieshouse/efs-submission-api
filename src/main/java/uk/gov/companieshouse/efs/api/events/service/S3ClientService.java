package uk.gov.companieshouse.efs.api.events.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component
public class S3ClientService {

    private S3Presigner presigner;
    private Long expiryInDays;
    private S3Client s3Client;
    private String envName;

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    @Autowired
    public S3ClientService(S3Presigner presigner, @Value("${file.link.expiry.in.days}") Long expiryInDays,
        S3Client s3Client, @Value("${env.name}") String envName) {
        this.presigner = presigner;
        this.expiryInDays = expiryInDays;
        this.s3Client = s3Client;
        this.envName = envName;
    }

    public void uploadToS3(final String reportName, final String csvContent, final String paymentReportBucketName) {
        try {
            LOGGER.infoContext(reportName, String.format("Uploading [%s] to S3", reportName), null);
            final PutObjectRequest request =
                    buildS3Request(reportName, paymentReportBucketName, "text/csv", StandardCharsets.UTF_8.name());
            final RequestBody body = setS3BodyContent(csvContent);

            final PutObjectResponse response = s3Client.putObject(request, body);
            LOGGER.infoContext(reportName, "Amazon S3 response: " + response, null);
        } catch (SdkException sdke) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("reportName", reportName);
            debug.put("paymentReportBucketName", paymentReportBucketName);
            LOGGER.error("Unable to upload report to S3", sdke, debug);
        }
    }

    public String generateFileLink(String fileId, String bucketName) {
        try {
            GetObjectPresignRequest objectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(expiryInDays))
                .getObjectRequest(getObjectRequest -> getObjectRequest.key(fileId).bucket(bucketName))
                .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(objectPresignRequest);
            String result = presignedGetObjectRequest.url().toString();
            LOGGER.debug("Pre-Signed URL: " + result);
            return result;
        }catch(SdkException ex){
            Map<String, Object> debug = new HashMap<>();
            debug.put("fileId", fileId);
            debug.put("bucketName", bucketName);

            LOGGER.error("Unable to generate S3 file link", ex, debug);
            throw ex;
        }
    }

    /*
     * Enable test stubbing of static method call
     */
    RequestBody setS3BodyContent(final String content) {
        return RequestBody.fromString(content);
    }

    /*
     * Enable test stubbing of static method call
     */
    PutObjectRequest buildS3Request(final String reportName, final String bucketName, final String contentType,
        final String contentEncoding) {
        return PutObjectRequest.builder().bucket(bucketName).contentType(contentType).contentEncoding(contentEncoding)
            .key(getResourceId(reportName)).build();
    }

    /**
     * Generating a resource link which has an environment name prefix.
     * @param resourceId - the resource ID to be prefixed
     * @return the prefixed resource ID
     */
    public String getResourceId(final String resourceId) {
        return envName + "/" + resourceId;
    }
}
