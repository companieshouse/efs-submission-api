package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateTest.TEMPLATE_ID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;


class PaymentTemplateServiceTest {
    private class TestPaymentTemplateServiceImpl implements PaymentTemplateService {}

    private PaymentTemplateService testService;

    @BeforeEach
    void setUp() {
        testService = new TestPaymentTemplateServiceImpl();
    }

    @Test
    void getTemplate() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getTemplate(TEMPLATE_ID.getFee(),
                        TEMPLATE_ID.getStartTimestamp()));

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void putTemplate() {
        final PaymentTemplate template = PaymentTemplate.newBuilder().build();

        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
            () -> testService.putTemplate(template));

        assertThat(thrown.getMessage(), is("not implemented"));
    }
}