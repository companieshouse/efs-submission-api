package uk.gov.companieshouse.efs.api.config;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.avro.io.EncoderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.email.config.ExternalConfirmationEmailConfig;
import uk.gov.companieshouse.efs.api.email.config.ExternalPaymentFailedEmailConfig;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalNotificationEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.interceptor.AuthenticationHelper;
import uk.gov.companieshouse.efs.api.interceptor.AuthenticationHelperImpl;
import uk.gov.companieshouse.efs.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.efs.api.paymentreports.service.OutputStreamWriterFactory;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;
import uk.gov.companieshouse.efs.api.util.UuidGenerator;
import uk.gov.companieshouse.logging.Logger;

/**
 * Configuration class used by api for validation and id generation.
 */
@Configuration
@PropertySource("classpath:validation.properties")
@EnableTransactionManagement
public class Config {
    private static final String REF_PATTERN = "####-####-####-####";
    private static final String ZBASE32_SYMBOLS = "abcdefghijkmnopqrstuwxyz13456789";

    @Value("${ref.pattern:" + REF_PATTERN + "}")
    private String pattern;
    @Value("${ref.symbol-set:" + ZBASE32_SYMBOLS + "}")
    private String symbolSet;
    @Value("${rng.algorithm}")
    private String algorithm;
    @Value("${rng.provider}")
    private String provider;
    @Value("${barcode.generator.service.url}")
    private String barcodeGeneratorServiceUrl;
    @Value("${file.bucket.name}")
    private String fileBucketName;
    @Value("${payment.report.bucket.name}")
    private String paymentReportBucketName;

    public Config() {
        // required no-arg constructor
    }

    /**
     * Obtains a clock that returns the current instant using the best available
     * system clock, converting to date and time using the UTC time-zone.
     *
     * @return a clock that uses the best available system clock in the UTC zone, not null
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public OutputStreamWriterFactory reportStreamWriterFactory() {
        return new OutputStreamWriterFactory();
    }

    @Bean
    SecureRandom secureRandom(@Qualifier("algorithm") final String algorithm,
        @Qualifier("provider") final String provider) throws NoSuchProviderException, NoSuchAlgorithmException {
        return SecureRandom.getInstance(algorithm, provider);
    }

    @Bean("ref-pattern")
    String pattern() {
        return pattern;
    }

    @Bean("ref-symbol-set")
    String symbolSet() {
        return symbolSet;
    }

    @Bean("algorithm")
    String algorithm() {
        return algorithm;
    }

    @Bean("provider")
    String provider() {
        return provider;
    }

    @Bean("barcodeGeneratorServiceUrl")
    String barcodeGeneratorServiceUrl() {
        return barcodeGeneratorServiceUrl;
    }

    @Bean
    S3Client s3Client() {
        return S3Client.create();
    }

    @Bean("file-bucket-name")
    String fileBucketName() {
        return fileBucketName;
    }

    @Bean("payment-report-bucket-name")
    String paymentReportBucketName() {
        return paymentReportBucketName;
    }

    /**
     * Return an immutable formatter capable of formatting and parsing
     * the ISO-8601 extended date format.
     *
     * @return {@link DateTimeFormatter} an ISO date formatter
     */
    @Bean
    public DateTimeFormatter isoFormatter() {
        return DateTimeFormatter.ISO_DATE;
    }

    /**
     * Returns an interceptor class for user authentication.
     *
     * @param authHelper helper class used for authenticating users
     * @param logger logging class
     * @return {@link UserAuthenticationInterceptor} interceptor class for user authentication
     */
    @Bean
    public UserAuthenticationInterceptor interceptor(final AuthenticationHelper authHelper, final Logger logger) {
        return new UserAuthenticationInterceptor(authHelper, logger);
    }

    /**
     * Returns a helper class for user authentication.
     *
     * @return {@link AuthenticationHelperImpl} helper class for user authentication
     */
    @Bean
    AuthenticationHelper authHelper() {
        return new AuthenticationHelperImpl();
    }

    /**
     * Create default response error handler for the rest templates.
     *
     * @return A default response error handler
     */
    @Bean
    ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

    /**
     * Create a rest template for service operations.
     *
     * @param handler A default response error handler
     * @return A rest template for the service operation.
     */
    @Bean
    RestTemplate restTemplate(final ResponseErrorHandler handler) {
        final RestTemplate template = new RestTemplateBuilder().build();
        template.setErrorHandler(handler);

        return template;
    }

    @Bean
    Supplier<UUID> uuidSource() {
        return UUID::randomUUID;
    }

    @Bean
    SqsClient sqsClient() {
        return SqsClient.create();
    }

    @Bean
    EncoderFactory encoderFactory() {
        return EncoderFactory.get();
    }

    @Bean
    @Primary
    @ConfigurationProperties("fes.datasource")
    public DataSourceProperties fesDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("chips.datasource")
    public DataSourceProperties chipsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("fesDataSource")
    @Primary
    @ConfigurationProperties("fes.datasource.configuration")
    DataSource fesDataSource() {
        return fesDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("chipsDataSource")
    @ConfigurationProperties("chips.datasource.configuration")
    DataSource chipsDataSource() {
        return chipsDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("fesJdbc")
    JdbcTemplate fesJdbcTemplate(@Qualifier("fesDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("chipsJdbc")
    JdbcTemplate chipsJdbcTemplate(@Qualifier("chipsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("fesTransactionManager")
    DataSourceTransactionManager fesTransactionManager(@Qualifier("fesDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("idGenerator")
    IdentifierGeneratable idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    S3Presigner presigner() {
        return S3Presigner.create();
    }

    @Bean("confirmationEmailMapper")
    ExternalNotificationEmailMapper confirmationEmailMapper(
        final ExternalConfirmationEmailConfig externalConfirmationEmailConfig,
        final TimestampGenerator<LocalDateTime> timestampGenerator,
        final CategoryTemplateService categoryTemplateService,
        final FormTemplateService formTemplateService, final IdentifierGeneratable idGenerator) {
        return new ExternalNotificationEmailMapper(externalConfirmationEmailConfig, idGenerator,
            timestampGenerator, categoryTemplateService, formTemplateService);
    }

    @Bean("paymentFailedEmailMapper")
    ExternalNotificationEmailMapper paymentFailedEmailMapper(
        final ExternalPaymentFailedEmailConfig externalPaymentFailedEmailConfig,
        final TimestampGenerator<LocalDateTime> timestampGenerator,
        final CategoryTemplateService categoryTemplateService,
        final FormTemplateService formTemplateService, final IdentifierGeneratable idGenerator) {
        return new ExternalNotificationEmailMapper(externalPaymentFailedEmailConfig, idGenerator,
            timestampGenerator, categoryTemplateService, formTemplateService);
    }

    @Bean
    EmailMapperFactory emailMapperFactory(
        final ExternalEmailMapperFactory externalEmailMapperFactory,
        final InternalEmailMapperFactory internalEmailMapperFactory) {


        return EmailMapperFactory.newBuilder()
            .withAcceptEmailMapper(externalEmailMapperFactory.getAcceptEmailMapper())
            .withConfirmationEmailMapper(externalEmailMapperFactory.getConfirmationMapper())
            .withPaymentFailedEmailMapper(externalEmailMapperFactory.getPaymentFailedMapper())
            .withDelayedSubmissionBusinessEmailMapper(
                internalEmailMapperFactory.getDelayedSubmissionBusinessMapper())
            .withDelayedSubmissionSupportEmailMapper(
                internalEmailMapperFactory.getDelayedSubmissionSupportMapper())
            .withInternalAvFailedEmailMapper(internalEmailMapperFactory.getInternalAvFailedMapper())
            .withInternalFailedConversionEmailMapper(
                internalEmailMapperFactory.getInternalFailedConversionMapper())
            .withInternalSubmissionEmailMapper(
                internalEmailMapperFactory.getInternalSubmissionMapper())
            .withPaymentReportEmailMapper(internalEmailMapperFactory.getPaymentReportMapper())
            .withRejectEmailMapper(externalEmailMapperFactory.getRejectMapper())
            .build();
    }

}
