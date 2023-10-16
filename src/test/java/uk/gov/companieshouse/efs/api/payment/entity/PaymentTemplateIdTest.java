package uk.gov.companieshouse.efs.api.payment.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentTemplateIdTest {

    private PaymentTemplateId testPaymentTemplateId;
    private static final String FEE = "Fee Template ID";
    private static final LocalDateTime ACTIVE = LocalDateTime.parse("2019-01-08T00:00");

    @BeforeEach
    void setUp() {
        testPaymentTemplateId = new PaymentTemplateId(FEE, ACTIVE);
    }

    @Test
    void paymentTemplateId() {
        PaymentTemplateId paymentTemplateId = new PaymentTemplateId();
        assertThat(paymentTemplateId.getFee(), is(nullValue()));
        assertThat(paymentTemplateId.getActiveFrom(), is(nullValue()));
    }

    @Test
    void paymentTemplateIdStringInstant() {
        assertThat(testPaymentTemplateId.getFee(), is(FEE));
        assertThat(testPaymentTemplateId.getActiveFrom(), is(ACTIVE));

    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(PaymentTemplateId.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void testToString() {
        assertThat(testPaymentTemplateId.toString(),
                //@formatter:off
                is("PaymentTemplateId["
                        + "fee=Fee Template ID,"
                        + "activeFrom=2019-01-08T00:00"
                        + "]"));
                //@formatter:off
    }
}