package uk.gov.companieshouse.efs.api.payment.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.service.PaymentTemplateService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PaymentFeatureTestingFeesControllerTest {
    @Mock
    private PaymentTemplateService paymentTemplateService;
    @Mock
    private Logger logger;
    private PaymentFeatureTestingFeesController controller;
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 4, 21, 12, 30);

    @BeforeEach
    void setUp() {
        controller = new PaymentFeatureTestingFeesController(paymentTemplateService, logger);
    }

    @Test
    void getPaymentTemplates_returnsAll_whenNoParams() {
        final var templates = List.of(PaymentTemplate.newBuilder().build());
        when(paymentTemplateService.getPaymentTemplates()).thenReturn(templates);

        final var response = controller.getPaymentTemplates(null, null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
    }

    @Test
    void getPaymentTemplates_returnsByFee_whenFeeOnly() {
        final var templates = List.of(PaymentTemplate.newBuilder().build());
        when(paymentTemplateService.getPaymentTemplates("FEE1")).thenReturn(templates);

        final var response = controller.getPaymentTemplates("FEE1", null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
    }

    @Test
    void getPaymentTemplates_returnsByFeeAndActiveAt_whenBothParams() {
        final var template = PaymentTemplate.newBuilder().build();
        when(paymentTemplateService.getPaymentTemplate(eq("FEE2"), any(LocalDateTime.class)))
            .thenReturn(java.util.Optional.of(template));

        final var response = controller.getPaymentTemplates("FEE2", FIXED_DATE_TIME);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(1));
    }

    @Test
    void getPaymentTemplates_returnsEmpty_whenNoTemplateFound() {
        when(paymentTemplateService.getPaymentTemplate(eq("FEE3"), any(LocalDateTime.class)))
            .thenReturn(java.util.Optional.empty());
        final var response = controller.getPaymentTemplates("FEE3", FIXED_DATE_TIME);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(Collections.emptyList()));
    }

    @Test
    void getPaymentTemplates_returns500_onException() {
        when(paymentTemplateService.getPaymentTemplates()).thenThrow(new RuntimeException("fail"));
        final var response = controller.getPaymentTemplates(null, null);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void createPaymentTemplate_returnsCreated() {
        final var template = PaymentTemplate.newBuilder()
            .withId(new PaymentTemplateId("FEE", FIXED_DATE_TIME))
            .build();
        when(paymentTemplateService.postTemplate(any())).thenReturn(template);
        final var dto = new PaymentFeatureTestingFeesController.TestingFeesPaymentTemplate("FEE", FIXED_DATE_TIME, "10.00");

        // Stub ServletRequestAttributes for ServletUriComponentsBuilder
        final var mockRequest = mock(HttpServletRequest.class);
        final RequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);
        try {
            final var response = controller.createPaymentTemplate(dto);
            assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
            assertThat(response.getBody(), is(notNullValue()));
            assertThat(response.getBody(), hasProperty("id", notNullValue()));
            assertThat(response.getBody().getId(), hasProperty("activeFrom", is(FIXED_DATE_TIME)));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

}
