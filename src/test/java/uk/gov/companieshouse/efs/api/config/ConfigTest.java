package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.email.config.ExternalConfirmationEmailConfig;
import uk.gov.companieshouse.efs.api.email.config.ExternalPaymentFailedEmailConfig;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSH19SameDaySubmissionSupportEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionBusinessEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionSupportEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalAcceptEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalNotificationEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalRejectEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalAvFailedEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.InternalFailedConversionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalSubmissionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.PaymentReportEmailMapper;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.interceptor.AuthenticationHelper;
import uk.gov.companieshouse.efs.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.efs.api.paymentreports.service.OutputStreamWriterFactory;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ConfigTest {
    private Config testConfig;

    @Mock
    private DataSource dataSource;
    @Mock
    private AuthenticationHelper authHelper;
    @Mock
    private Logger logger;
    @Mock
    private ExternalConfirmationEmailConfig externalConfirmationEmailConfig;
    @Mock
    private ExternalPaymentFailedEmailConfig externalPaymentFailedEmailConfig;
    @Mock
    private TimestampGenerator<LocalDateTime> timestampGenerator;
    @Mock
    private CategoryTemplateService categoryTemplateService;
    @Mock
    private FormTemplateService formTemplateService;
    @Mock
    private IdentifierGeneratable identifierGeneratable;

    @BeforeEach
    void setUp() {
        testConfig = new Config();
    }

    @Test
    void constructor() {
        assertThat(testConfig.algorithm(), is(nullValue()));
        assertThat(testConfig.provider(), is(nullValue()));
        assertThat(testConfig.pattern(), is(nullValue()));
        assertThat(testConfig.symbolSet(), is(nullValue()));
        assertThat(testConfig.fileBucketName(), is(nullValue()));
        assertThat(testConfig.paymentReportBucketName(), is(nullValue()));
        assertThat(testConfig.barcodeGeneratorServiceUrl(), is(nullValue()));
    }

    @Test
    void clock() {
        assertThat(testConfig.clock(), isA(Clock.class));
    }

    @Test
    void idGenerator() {
        assertThat(testConfig.idGenerator(), isA(IdentifierGeneratable.class));
    }

    @Test
    void presigner() {
        assertThat(testConfig.presigner(), isA(S3Presigner.class));
    }

    @Test
    void s3Client() {
        assertThat(testConfig.s3Client(), isA(S3Client.class));
    }

    @Test
    void sqsClient() {
        assertThat(testConfig.sqsClient(), isA(SqsClient.class));
    }

    @Test
    void reportStreamWriterFactory() {
        assertThat(testConfig.reportStreamWriterFactory(), isA(OutputStreamWriterFactory.class));
    }

    @Test
    void secureRandom() throws Exception {
        assertThat(testConfig.secureRandom("SHA1PRNG", "SUN"), isA(java.security.SecureRandom.class));
    }

    @Test
    void propertyBackedBeans() {
        ReflectionTestUtils.setField(testConfig, "pattern", "ABCD");
        ReflectionTestUtils.setField(testConfig, "symbolSet", "xyz");
        ReflectionTestUtils.setField(testConfig, "algorithm", "ALG");
        ReflectionTestUtils.setField(testConfig, "provider", "PROV");
        ReflectionTestUtils.setField(testConfig, "barcodeGeneratorServiceUrl", "http://barcode");
        ReflectionTestUtils.setField(testConfig, "fileBucketName", "files-bucket");
        ReflectionTestUtils.setField(testConfig, "paymentReportBucketName", "reports-bucket");

        assertThat(testConfig.pattern(), is("ABCD"));
        assertThat(testConfig.symbolSet(), is("xyz"));
        assertThat(testConfig.algorithm(), is("ALG"));
        assertThat(testConfig.provider(), is("PROV"));
        assertThat(testConfig.barcodeGeneratorServiceUrl(), is("http://barcode"));
        assertThat(testConfig.fileBucketName(), is("files-bucket"));
        assertThat(testConfig.paymentReportBucketName(), is("reports-bucket"));
    }

    @Test
    void isoFormatter() {
        assertThat(testConfig.isoFormatter(), is(DateTimeFormatter.ISO_DATE));
    }

    @Test
    void authHelper() {
        assertThat(testConfig.authHelper(), isA(AuthenticationHelper.class));
    }

    @Test
    void restClient() {
        assertThat(testConfig.restClient(), isA(org.springframework.web.client.RestClient.class));
    }

    @Test
    void uuidSource() {
        var uuidSource = testConfig.uuidSource();

        assertThat(uuidSource.get(), isA(UUID.class));
    }

    @Test
    void encoderFactory() {
        assertThat(testConfig.encoderFactory(), isA(org.apache.avro.io.EncoderFactory.class));
    }

    @Test
    void dataSourcePropertiesBeans() {
        assertThat(testConfig.fesDataSourceProperties(), isA(DataSourceProperties.class));
        assertThat(testConfig.chipsDataSourceProperties(), isA(DataSourceProperties.class));
    }


    @Test
    void jdbcBeans() {
        assertThat(testConfig.fesJdbcTemplate(dataSource), isA(JdbcTemplate.class));
        assertThat(testConfig.chipsJdbcTemplate(dataSource), isA(JdbcTemplate.class));
    }

    @Test
    void fesTransactionManager() {
        assertThat(testConfig.fesTransactionManager(dataSource), isA(DataSourceTransactionManager.class));
    }

    @Test
    void interceptor() {
        assertThat(testConfig.interceptor(authHelper, logger), isA(UserAuthenticationInterceptor.class));
    }

    @Test
    void externalNotificationMappers() {
        assertThat(testConfig.confirmationEmailMapper(
            externalConfirmationEmailConfig,
            timestampGenerator,
            categoryTemplateService,
            formTemplateService,
            identifierGeneratable), isA(ExternalNotificationEmailMapper.class));

        assertThat(testConfig.paymentFailedEmailMapper(
            externalPaymentFailedEmailConfig,
            timestampGenerator,
            categoryTemplateService,
            formTemplateService,
            identifierGeneratable), isA(ExternalNotificationEmailMapper.class));
    }

    @Test
    void emailMapperFactory() {
        final var acceptEmailMapper = new ExternalAcceptEmailMapper(null, null, null);
        final var confirmationMapper =
            new ExternalNotificationEmailMapper(null, null, null, null, null);
        final var paymentFailedMapper =
            new ExternalNotificationEmailMapper(null, null, null, null, null);
        final var rejectMapper = new ExternalRejectEmailMapper(null, null, null);
        final var externalMapperFactory =
            new ExternalEmailMapperFactory(acceptEmailMapper, confirmationMapper,
                paymentFailedMapper, rejectMapper);
        final var delayedSubmissionBusinessMapper =
            new DelayedSubmissionBusinessEmailMapper(null, null, null);
        final var delayedSubmissionSupportMapper =
            new DelayedSubmissionSupportEmailMapper(null, null, null);
        final var delayedSH19SameDaySubmissionSupportEmailMapper =
            new DelayedSH19SameDaySubmissionSupportEmailMapper(null, null, null);
        final var internalAvFailedMapper =
            new InternalAvFailedEmailMapper(null, null, null, null);
        final var internalFailedConversionMapper =
            new InternalFailedConversionEmailMapper(null, null, null, null);
        final var internalSubmissionMapper =
            new InternalSubmissionEmailMapper(null, null, null, null, null, null);
        final var paymentReportMapper = new PaymentReportEmailMapper(null, null, null);
        final var internalMapperFactory =
            new InternalEmailMapperFactory(delayedSubmissionBusinessMapper,
                delayedSubmissionSupportMapper, delayedSH19SameDaySubmissionSupportEmailMapper,
                internalAvFailedMapper,
                internalFailedConversionMapper, internalSubmissionMapper,
                paymentReportMapper);

        final var mapperFactory =
            testConfig.emailMapperFactory(externalMapperFactory, internalMapperFactory);

        assertThat(mapperFactory, isA(EmailMapperFactory.class));
        assertThat(mapperFactory.getAcceptEmailMapper(), isA(ExternalAcceptEmailMapper.class));
        assertThat(mapperFactory.getConfirmationEmailMapper(), isA(ExternalNotificationEmailMapper.class));
        assertThat(mapperFactory.getPaymentFailedEmailMapper(), isA(ExternalNotificationEmailMapper.class));
        assertThat(mapperFactory.getRejectEmailMapper(), isA(ExternalRejectEmailMapper.class));
        assertThat(mapperFactory.getDelayedSubmissionBusinessEmailMapper(), isA(DelayedSubmissionBusinessEmailMapper.class));
        assertThat(mapperFactory.getDelayedSH19SameDaySubmissionSupportEmailMapper(),
            isA(DelayedSH19SameDaySubmissionSupportEmailMapper.class));
        assertThat(mapperFactory.getDelayedSubmissionSupportEmailMapper(), isA(DelayedSubmissionSupportEmailMapper.class));
        assertThat(mapperFactory.getInternalAvFailedEmailMapper(), isA(InternalAvFailedEmailMapper.class));
        assertThat(mapperFactory.getInternalFailedConversionEmailMapper(), isA(InternalFailedConversionEmailMapper.class));
        assertThat(mapperFactory.getInternalSubmissionEmailMapper(), isA(InternalSubmissionEmailMapper.class));
        assertThat(mapperFactory.getPaymentReportEmailMapper(), isA(PaymentReportEmailMapper.class));
    }

    @Test
    void internalApiClient()  {
        var supplier = testConfig.internalApiClientSupplier("api-key", "http://localhost");

        var apiClient = supplier.get();

        assertThat(apiClient, isA(InternalApiClient.class));
        assertThat(supplier.get(), is(apiClient));
    }

}