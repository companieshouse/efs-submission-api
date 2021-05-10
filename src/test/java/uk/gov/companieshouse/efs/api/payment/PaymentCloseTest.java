package uk.gov.companieshouse.efs.api.payment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentCloseTest {
    public static final String SESSION_ID = "2222222222";

    private PaymentClose testClose;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testClose = new PaymentClose(SESSION_ID, PaymentClose.Status.PAID, now);
    }

    @Test
    void paymentCloseStringStatus() {
        testClose = new PaymentClose(SESSION_ID, PaymentClose.Status.FAILED);
        
        assertThat(testClose.getPaymentReference(), is(SESSION_ID));
        assertThat(testClose.getStatus(), is(PaymentClose.Status.FAILED.toString()));
    }
    
    @Test
    void getPaidAt() {
        assertThat(testClose.getPaidAt(), is(now));
    }

    @Test
    void setPaidAt() {
        final LocalDateTime expected = now.plusSeconds(5);
        
        testClose.setPaidAt(expected);
        
        assertThat(testClose.getPaidAt(), is(expected));
    }

    @Test
    void getPaymentReference() {
        assertThat(testClose.getPaymentReference(), is(SESSION_ID));
    }

    @Test
    void setPaymentReference() {
        final String expected = "new-ref";
        
        testClose.setPaymentReference(expected);
        
        assertThat(testClose.getPaymentReference(), is(expected));
    }

    @Test
    void getStatus() {
        assertThat(testClose.getStatus(), is(PaymentClose.Status.PAID.toString()));
    }

    @Test
    void setStatus() {
        final String expected = PaymentClose.Status.FAILED.toString();
        
        testClose.setStatus(expected);
        
        assertThat(testClose.getStatus(), is(expected));
        assertThat(testClose.isFailed(), is(true));
    }

    @Test
    void isPaid() {
        assertThat(testClose.isPaid(), is(true));
    }

    @Test
    void isFailed() {
        assertThat(testClose.isFailed(), is(false));
    }
    
    @Test
    void statusFromValueWhenNotMatched() {
        assertThat(PaymentClose.Status.fromValue("no-match"), is(nullValue()));
    }
}