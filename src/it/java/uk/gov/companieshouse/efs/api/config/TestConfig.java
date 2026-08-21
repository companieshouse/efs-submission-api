package uk.gov.companieshouse.efs.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class TestConfig {

    @Bean("testSqsClient")
    @Primary
    SqsClient sqsClient(@Value("${sqs.endpoint.override}") String sqsEndpointOverride,
                        @Value("${aws.region}") String awsRegion) {
        return SqsClient.builder()
                .endpointOverride(URI.create(sqsEndpointOverride))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .build();

    }

    @Bean("testS3Client")
    @Primary
    S3Client s3Client(@Value("${s3.endpoint.override}") String s3EndpointOverride,
                      @Value("${aws.region}") String awsRegion) {
        return S3Client.builder()
                .endpointOverride(URI.create(s3EndpointOverride))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .build();
    }
}
