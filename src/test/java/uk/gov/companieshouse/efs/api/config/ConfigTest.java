package uk.gov.companieshouse.efs.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.efs.api.email.mapper.*;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;

import java.time.Clock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@ExtendWith(MockitoExtension.class)
class ConfigTest {
    private Config testConfig;

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
    void emailMapperFactory() {
        final ExternalAcceptEmailMapper acceptEmailMapper = new ExternalAcceptEmailMapper(null, null, null);
        final ExternalNotificationEmailMapper confirmationMapper =
            new ExternalNotificationEmailMapper(null, null, null, null, null);
        final ExternalNotificationEmailMapper paymentFailedMapper =
            new ExternalNotificationEmailMapper(null, null, null, null, null);
        final ExternalRejectEmailMapper rejectMapper = new ExternalRejectEmailMapper(null, null, null);
        final ExternalEmailMapperFactory externalMapperFactory =
            new ExternalEmailMapperFactory(acceptEmailMapper, confirmationMapper,
                paymentFailedMapper, rejectMapper);
        final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper =
            new DelayedSubmissionBusinessEmailMapper(null, null, null);
        final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper =
            new DelayedSubmissionSupportEmailMapper(null, null, null);
        final DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper =
            new DelayedSH19SameDaySubmissionSupportEmailMapper(null, null, null);
        final InternalAvFailedEmailMapper internalAvFailedMapper =
            new InternalAvFailedEmailMapper(null, null, null, null);
        final InternalFailedConversionEmailMapper internalFailedConversionMapper =
            new InternalFailedConversionEmailMapper(null, null, null, null);
        final InternalSubmissionEmailMapper internalSubmissionMapper =
            new InternalSubmissionEmailMapper(null, null, null, null, null, null);
        final PaymentReportEmailMapper paymentReportMapper = new PaymentReportEmailMapper(null, null, null);
        final InternalEmailMapperFactory internalMapperFactory =
            new InternalEmailMapperFactory(delayedSubmissionBusinessMapper,
                delayedSubmissionSupportMapper, delayedSH19SameDaySubmissionSupportEmailMapper,
                internalAvFailedMapper,
                internalFailedConversionMapper, internalSubmissionMapper,
                paymentReportMapper);

        final EmailMapperFactory mapperFactory =
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
        InternalApiClient apiClient = testConfig.internalApiClientSupplier("api-key", "http://localhost").get();
        assertThat(apiClient, isA(InternalApiClient.class));
    }

}