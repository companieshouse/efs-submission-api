package uk.gov.companieshouse.efs.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class TestConfig {

    @Bean("testSqsClient")
    @Primary
    SqsClient sqsClient(@Value("${sqs.endpoint.override}") String sqsEndpointOverride,
                        @Value("${aws.access.key}") String awsAccessKey,
                        @Value("${aws.secret.key}") String awsSecretKey,
                        @Value("${aws.region}") String awsRegion) {
        return SqsClient.builder()
                .endpointOverride(URI.create(sqsEndpointOverride))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .region(Region.of(awsRegion))
                .build();

    }

    @Bean("testS3Client")
    @Primary
    S3Client s3Client(@Value("${s3.endpoint.override}") String s3EndpointOverride,
                      @Value("${aws.access.key}") String awsAccessKey,
                      @Value("${aws.secret.key}") String awsSecretKey,
                      @Value("${aws.region}") String awsRegion) {
        return S3Client.builder()
                .endpointOverride(URI.create(s3EndpointOverride))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .region(Region.of(awsRegion))
                .build();
    }
}
