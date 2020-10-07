package uk.gov.companieshouse.efs.api.submissions.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.efs.api.submissions.model.PaymentSession;

@ExtendWith(MockitoExtension.class)
class PaymentSessionMapperTest {

    public static final String SESSION_ID = "1234567890";
    public static final String SESSION_STATE = "FD_RlzcLp-xcK1YZGEbn3ZpRHGlwy7tNjn_zsjYVauoB8Ml3GkfpmbhPuPd093XM";

    private PaymentSessionMapper paymentSessionMapper;

    @Mock
    private SessionApi paymentSessionApi;

    @BeforeEach
    void setUp(){
        this.paymentSessionMapper = new PaymentSessionMapper();
    }

    @Test
    void testPaymentReferenceMapperReturnsPaymentSessionRepresentation() {
        //given
        when(paymentSessionApi.getSessionId()).thenReturn(SESSION_ID);
        when(paymentSessionApi.getSessionState()).thenReturn(SESSION_STATE);

        //when
        PaymentSession actual = paymentSessionMapper.map(paymentSessionApi);

        //then
        assertThat(actual, is(equalTo(new PaymentSession(SESSION_ID, SESSION_STATE))));
    }
}
