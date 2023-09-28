package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;


class PaymentTemplateServiceTest {
    public static final PaymentTemplateId TEMPLATE_ID =
        new PaymentTemplateId("SLPCS01 Test", LocalDateTime.parse("2019-01-08T00:00:00"));

    private class TestPaymentTemplateServiceImpl implements PaymentTemplateService {}

    private PaymentTemplateService testService;

    @BeforeEach
    void setUp() {
        testService = new TestPaymentTemplateServiceImpl();
    }

    @Test
    void getTemplate() {
        final String fee = TEMPLATE_ID.getFee();
        final LocalDateTime activeFrom = TEMPLATE_ID.getActiveFrom();
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
            () -> testService.getTemplate(fee, activeFrom));

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