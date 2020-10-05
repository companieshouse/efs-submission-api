package uk.gov.companieshouse.efs.api.events.service;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.gov.companieshouse.efs.api.events.service.exception.TiffDownloadException;

@Component
public class TiffDownloadServiceImpl implements TiffDownloadService {

    private S3Client s3;

    private String bucketName;

    @Autowired
    public TiffDownloadServiceImpl(S3Client that,
                                   @Value("${tiff.bucket.name}") String bucketName) {
        this.s3 = that;
        this.bucketName = bucketName;
    }

    @Override
    public byte[] downloadTiffFile(String fileId) {
        try {
            return IOUtils.toByteArray(s3.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(String.format("%s/%s", "converted-tiffs", fileId))
                    .build()));
        } catch (IOException ex) {
            throw new TiffDownloadException("Failed to convert TIFF to byteArray", ex);
        } catch (SdkException se) {
            throw new TiffDownloadException("Failed to download TIFF", se);
        }
    }
}

