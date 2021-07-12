package uk.gov.companieshouse.efs.api.email.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalEmailMapperFactoryTest {
    private InternalEmailMapperFactory testFactory;

    @Mock
    private DelayedSubmissionBusinessEmailMapper delayedBusinessMapper;
    @Mock
    private DelayedSubmissionSupportEmailMapper delayedSupportMapper;
    @Mock
    private DelayedSH19SameDaySubmissionSupportEmailMapper
        delayedSH19SameDaySubmissionSupportEmailMapper;
    @Mock
    private DelayedSH19SameDaySubmissionBusinessEmailMapper
        delayedSH19SameDaySubmissionBusinessEmailMapper;
    @Mock
    private InternalAvFailedEmailMapper avFailedMapper;
    @Mock
    private InternalFailedConversionEmailMapper failedConversionMapper;
    @Mock
    private InternalSubmissionEmailMapper submissionMapper;
    @Mock
    private PaymentReportEmailMapper paymentReportMapper;


    @BeforeEach
    void setUp() {
        testFactory = new InternalEmailMapperFactory(delayedBusinessMapper, delayedSupportMapper,
            delayedSH19SameDaySubmissionBusinessEmailMapper, delayedSH19SameDaySubmissionSupportEmailMapper,
            avFailedMapper, failedConversionMapper, submissionMapper, paymentReportMapper);
    }

    @Test
    void getDelayedSubmissionBusinessMapper() {
        assertThat(testFactory.getDelayedSubmissionBusinessMapper(), is(sameInstance(delayedBusinessMapper)));
    }

    @Test
    void getDelayedSubmissionSupportMapper() {
        assertThat(testFactory.getDelayedSubmissionSupportMapper(), is(sameInstance(delayedSupportMapper)));
    }

    @Test
    void getInternalAvFailedMapper() {
        assertThat(testFactory.getInternalAvFailedMapper(), is(sameInstance(avFailedMapper)));
    }

    @Test
    void getInternalFailedConversionMapper() {
        assertThat(testFactory.getInternalFailedConversionMapper(), is(sameInstance(failedConversionMapper)));
    }

    @Test
    void getInternalSubmissionMapper() {
        assertThat(testFactory.getInternalSubmissionMapper(), is(sameInstance(submissionMapper)));
    }

    @Test
    void getPaymentReportMapper() {
        assertThat(testFactory.getPaymentReportMapper(), is(sameInstance(paymentReportMapper)));
    }
}