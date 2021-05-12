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
class ExternalEmailMapperFactoryTest {
    private ExternalEmailMapperFactory testFactory;

    @Mock
    private ExternalAcceptEmailMapper acceptMapper;
    @Mock
    private ExternalNotificationEmailMapper confirmationMapper;
    @Mock
    private ExternalNotificationEmailMapper paymentFailedMapper;
    @Mock
    private ExternalRejectEmailMapper rejectMapper;

    @BeforeEach
    void setUp() {
        testFactory = new ExternalEmailMapperFactory(acceptMapper, confirmationMapper,
            paymentFailedMapper, rejectMapper);
    }

    @Test
    void getAcceptEmailMapper() {
        assertThat(testFactory.getAcceptEmailMapper(), is(sameInstance(acceptMapper)));
    }

    @Test
    void getConfirmationMapper() {
        assertThat(testFactory.getConfirmationMapper(), is(sameInstance(confirmationMapper)));
    }

    @Test
    void getPaymentFailedMapper() {
        assertThat(testFactory.getPaymentFailedMapper(), is(sameInstance(paymentFailedMapper)));
    }

    @Test
    void getRejectMapper() {
        assertThat(testFactory.getRejectMapper(), is(sameInstance(rejectMapper)));
    }
}