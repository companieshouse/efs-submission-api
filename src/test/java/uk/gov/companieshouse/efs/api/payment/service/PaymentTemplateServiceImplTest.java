package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateTest.TEMPLATE_ID;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;

@ExtendWith(MockitoExtension.class)
class PaymentTemplateServiceImplTest {

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
    void getTemplate() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();

        when(repository.findFirstById_FeeAndId_StartTimestampLessThanEqualOrderById_StartTimestampDesc(
                TEMPLATE_ID.getFee(), TEMPLATE_ID.getStartTimestamp())).thenReturn(
                Optional.of(expected));

        testService.getTemplate(TEMPLATE_ID.getFee(), TEMPLATE_ID.getStartTimestamp());

        verify(repository).findFirstById_FeeAndId_StartTimestampLessThanEqualOrderById_StartTimestampDesc(
                TEMPLATE_ID.getFee(), TEMPLATE_ID.getStartTimestamp());
    }

    @DisplayName("GIVEN a payment template to store "
                 + "WHEN put template using the service "
                 + "THEN the object is stored")
    @Test
    void putTemplate() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();
        testService.putTemplate(expected);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId(), is(TEMPLATE_ID));
    }
}