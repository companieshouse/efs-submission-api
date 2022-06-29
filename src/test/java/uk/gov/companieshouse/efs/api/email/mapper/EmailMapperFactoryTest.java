package uk.gov.companieshouse.efs.api.email.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailMapperFactoryTest {
    private EmailMapperFactory testFactory;

    @Mock
    private ExternalNotificationEmailMapper confirmationEmailMapper;
    @Mock
    private ExternalNotificationEmailMapper paymentFailedEmailMapper;
    @Mock
    private ExternalAcceptEmailMapper acceptEmailMapper;
    @Mock
    private ExternalRejectEmailMapper rejectEmailMapper;
    @Mock
    private InternalAvFailedEmailMapper internalAVFailedEmailMapper;
    @Mock
    private InternalFailedConversionEmailMapper internalFailedConversionEmailMapper;
    @Mock
    private InternalSubmissionEmailMapper internalSubmissionEmailMapper;
    @Mock
    private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;
    @Mock
    private DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper;
    @Mock
    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;
    @Mock
    private PaymentReportEmailMapper paymentReportEmailMapper;

    @BeforeEach
    void setUp() {
        testFactory = EmailMapperFactory.newBuilder()
            .withConfirmationEmailMapper(confirmationEmailMapper)
            .withPaymentFailedEmailMapper(paymentFailedEmailMapper)
            .withAcceptEmailMapper(acceptEmailMapper).withRejectEmailMapper(rejectEmailMapper)
            .withInternalAvFailedEmailMapper(internalAVFailedEmailMapper)
            .withInternalFailedConversionEmailMapper(internalFailedConversionEmailMapper)
            .withInternalSubmissionEmailMapper(internalSubmissionEmailMapper)
            .withDelayedSubmissionSupportEmailMapper(delayedSubmissionSupportEmailMapper)
            .withDelayedSH19SameDaySubmissionSupportEmailMapper(
                delayedSH19SameDaySubmissionSupportEmailMapper)
            .withDelayedSubmissionBusinessEmailMapper(delayedSubmissionBusinessEmailMapper)
            .withPaymentReportEmailMapper(paymentReportEmailMapper).build();
    }

    @Test
    void buildWhenConfirmationMapperNull() {
        final EmailMapperFactory.Builder builder =
                EmailMapperFactory.newBuilder().withAcceptEmailMapper(acceptEmailMapper)
                        .withPaymentFailedEmailMapper(paymentFailedEmailMapper)
                        .withRejectEmailMapper(rejectEmailMapper).withInternalAvFailedEmailMapper(internalAVFailedEmailMapper)
                        .withInternalFailedConversionEmailMapper(internalFailedConversionEmailMapper)
                        .withInternalSubmissionEmailMapper(internalSubmissionEmailMapper)
                        .withDelayedSubmissionSupportEmailMapper(delayedSubmissionSupportEmailMapper)
                        .withDelayedSubmissionBusinessEmailMapper(delayedSubmissionBusinessEmailMapper)
                        .withPaymentReportEmailMapper(paymentReportEmailMapper);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.build());

        assertThat(exception.getMessage(), is("'confirmationEmailMapper' must not be null"));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EmailMapperFactory.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void getConfirmationEmailMapper() {
        assertThat(testFactory.getConfirmationEmailMapper(),
            isA(ExternalNotificationEmailMapper.class));
    }

    @Test
    void getAcceptEmailMapper() {
        assertThat(testFactory.getAcceptEmailMapper(), isA(ExternalAcceptEmailMapper.class));
    }

    @Test
    void getRejectEmailMapper() {
        assertThat(testFactory.getRejectEmailMapper(), isA(ExternalRejectEmailMapper.class));
    }

    @Test
    void getInternalAVFailedEmailMapper() {
        assertThat(testFactory.getInternalAvFailedEmailMapper(), isA(InternalAvFailedEmailMapper.class));
    }

    @Test
    void getInternalFailedConversionEmailMapper() {
        assertThat(testFactory.getInternalFailedConversionEmailMapper(),
            isA(InternalFailedConversionEmailMapper.class));
    }

    @Test
    void getInternalSubmissionEmailMapper() {
        assertThat(testFactory.getInternalSubmissionEmailMapper(), isA(InternalSubmissionEmailMapper.class));
    }

    @Test
    void getDelayedSubmissionSupportEmailMapper() {
        assertThat(testFactory.getDelayedSubmissionSupportEmailMapper(),
            isA(DelayedSubmissionSupportEmailMapper.class));
    }

    @Test
    void getDelayedSubmissionBusinessEmailMapper() {
        assertThat(testFactory.getDelayedSubmissionBusinessEmailMapper(),
            isA(DelayedSubmissionBusinessEmailMapper.class));
    }

    @Test
    void getPaymentReportEmailMapper() {
        assertThat(testFactory.getPaymentReportEmailMapper(), isA(PaymentReportEmailMapper.class));
    }
}