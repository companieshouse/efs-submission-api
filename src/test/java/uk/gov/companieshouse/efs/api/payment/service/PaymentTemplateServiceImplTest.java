package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;

@ExtendWith(MockitoExtension.class)
class PaymentTemplateServiceImplTest {
    public static final PaymentTemplateId TEMPLATE_ID =
        new PaymentTemplateId("SLPCS01 Test", LocalDateTime.parse("2019-01-08T00:00:00"));

    private PaymentTemplateService testService;

    @Mock
    private PaymentTemplateRepository repository;
    private ArgumentCaptor<PaymentTemplate> captor;

    @BeforeEach
    void setUp() {
        testService = new PaymentTemplateServiceImpl(repository);
        captor = ArgumentCaptor.forClass(PaymentTemplate.class);
    }

    @DisplayName("GIVEN a template ID to retrieve an existing payment template "
            + "WHEN fetch template by ID using the service "
            + "THEN the result is the found object")
    @Test
    void getPaymentTemplateStringLocalDateTime() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();

        when(repository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEMPLATE_ID.getFee(), TEMPLATE_ID.getActiveFrom())).thenReturn(
                Optional.of(expected));

        testService.getPaymentTemplate(TEMPLATE_ID.getFee(), TEMPLATE_ID.getActiveFrom());

        verify(repository).findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                TEMPLATE_ID.getFee(), TEMPLATE_ID.getActiveFrom());
    }
    @DisplayName("GIVEN a template ID to retrieve existing payment templates "
            + "WHEN fetch templates by ID using the service "
            + "THEN the result is the found object")
    @Test
    void getPaymentTemplatesString() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();

        when(repository.findById_FeeOrderById_ActiveFromDesc(
                TEMPLATE_ID.getFee())).thenReturn(Collections.singletonList(expected));

        testService.getPaymentTemplates(TEMPLATE_ID.getFee());

        verify(repository).findById_FeeOrderById_ActiveFromDesc(
                TEMPLATE_ID.getFee());
    }
    @DisplayName("GIVEN there are payment templates "
            + "WHEN fetch templates by using the service "
            + "THEN the result is the found object")
    @Test
    void getPaymentTemplates() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();

        when(repository.findAll()).thenReturn(Collections.singletonList(expected));

        testService.getPaymentTemplates();

        verify(repository).findAll();
    }



    @DisplayName("GIVEN a payment template to store "
                 + "WHEN put template using the service "
                 + "THEN the object is stored")
    @Test
    void postTemplate() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();
        testService.postTemplate(expected);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId(), is(TEMPLATE_ID));
    }
}