package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionBusinessEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.DelayedSubmissionSupportEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.EmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalAcceptEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalConfirmationEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.ExternalRejectEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalAvFailedEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalEmailMapperFactory;
import uk.gov.companieshouse.efs.api.email.mapper.InternalFailedConversionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.InternalSubmissionEmailMapper;
import uk.gov.companieshouse.efs.api.email.mapper.PaymentReportEmailMapper;
import uk.gov.companieshouse.efs.api.paymentreports.service.OutputStreamWriterFactory;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;

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
        assertThat(testConfig.paymentReportBucketName(), is(nullValue()));
    }

    @Test
    void clock() {
        assertThat(testConfig.clock(), isA(Clock.class));
    }

    @Test
    void reportStreamWriterFactory() {
        assertThat(testConfig.reportStreamWriterFactory(), isA(OutputStreamWriterFactory.class));
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
        final ExternalConfirmationEmailMapper confirmationMapper =
            new ExternalConfirmationEmailMapper(null, null, null);
        final ExternalRejectEmailMapper rejectMapper = new ExternalRejectEmailMapper(null, null, null);
        final ExternalEmailMapperFactory externalMapperFactory =
            new ExternalEmailMapperFactory(acceptEmailMapper, confirmationMapper, rejectMapper);
        final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessMapper =
            new DelayedSubmissionBusinessEmailMapper(null, null, null);
        final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportMapper =
            new DelayedSubmissionSupportEmailMapper(null, null, null);
        final InternalAvFailedEmailMapper internalAvFailedMapper =
            new InternalAvFailedEmailMapper(null, null, null, null);
        final InternalFailedConversionEmailMapper internalFailedConversionMapper =
            new InternalFailedConversionEmailMapper(null, null, null, null);
        final InternalSubmissionEmailMapper internalSubmissionMapper =
            new InternalSubmissionEmailMapper(null, null, null, null);
        final PaymentReportEmailMapper paymentReportMapper = new PaymentReportEmailMapper(null, null, null);
        final InternalEmailMapperFactory internalMapperFactory =
            new InternalEmailMapperFactory(delayedSubmissionBusinessMapper,
                delayedSubmissionSupportMapper, internalAvFailedMapper,
                internalFailedConversionMapper, internalSubmissionMapper,
                paymentReportMapper);

        final EmailMapperFactory mapperFactory =
            testConfig.emailMapperFactory(externalMapperFactory, internalMapperFactory);

        assertThat(mapperFactory, isA(EmailMapperFactory.class));
        assertThat(mapperFactory.getAcceptEmailMapper(), isA(ExternalAcceptEmailMapper.class));
        assertThat(mapperFactory.getConfirmationEmailMapper(), isA(ExternalConfirmationEmailMapper.class));
        assertThat(mapperFactory.getRejectEmailMapper(), isA(ExternalRejectEmailMapper.class));
        assertThat(mapperFactory.getDelayedSubmissionBusinessEmailMapper(), isA(DelayedSubmissionBusinessEmailMapper.class));
        assertThat(mapperFactory.getDelayedSubmissionSupportEmailMapper(), isA(DelayedSubmissionSupportEmailMapper.class));
        assertThat(mapperFactory.getInternalAvFailedEmailMapper(), isA(InternalAvFailedEmailMapper.class));
        assertThat(mapperFactory.getInternalFailedConversionEmailMapper(), isA(InternalFailedConversionEmailMapper.class));
        assertThat(mapperFactory.getInternalSubmissionEmailMapper(), isA(InternalSubmissionEmailMapper.class));
        assertThat(mapperFactory.getPaymentReportEmailMapper(), isA(PaymentReportEmailMapper.class));
    }

}