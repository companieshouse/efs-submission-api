package uk.gov.companieshouse.efs.api.payment.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class TestingFeesPaymentTemplateTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    @Test
    void testRecordFieldsAndToString() {
        final var fee = "FEE123";
        final var activeFrom = LocalDateTime.of(2026, 4, 21, 12, 30);
        final var amount = "100.00";
        final var template = new PaymentFeatureTestingFeesController.TestingFeesPaymentTemplate(fee, activeFrom,
            amount);

        assertThat(template.fee(), is(fee));
        assertThat(template.activeFrom(), is(activeFrom));
        assertThat(template.amount(), is(amount));
        assertThat(template.toString(),
            is("TestingFeesPaymentTemplate[fee='FEE123', activeFrom=2026-04-21T12:30, amount='100.00']"));
    }

    @Test
    void testJsonSerialization() throws JacksonException {
        final var template = new PaymentFeatureTestingFeesController.TestingFeesPaymentTemplate("FEE456",
            LocalDateTime.of(2026, 4, 22, 8, 0), "200.00");
        final var json = OBJECT_MAPPER.writeValueAsString(template);
        assertThat(json, containsString("\"fee\":\"FEE456\""));
        assertThat(json, containsString("\"active_from\":\"2026-04-22T08:00:00\""));
        assertThat(json, containsString("\"amount\":\"200.00\""));
    }

    @Test
    void testJsonDeserialization() throws JacksonException {
        final var json = """
            {
            "fee":"FEE789",
            "active_from":"2026-04-23T09:15:00",
            "amount":"300.00"
            }""";
        final var template = OBJECT_MAPPER.readValue(json,
            PaymentFeatureTestingFeesController.TestingFeesPaymentTemplate.class);
        assertThat(template.fee(), is("FEE789"));
        assertThat(template.activeFrom(), is(LocalDateTime.of(2026, 4, 23, 9, 15)));
        assertThat(template.amount(), is("300.00"));
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier
            .forClass(PaymentFeatureTestingFeesController.TestingFeesPaymentTemplate.class)
            .withNonnullFields("fee", "activeFrom", "amount")
            .verify();
    }
}
