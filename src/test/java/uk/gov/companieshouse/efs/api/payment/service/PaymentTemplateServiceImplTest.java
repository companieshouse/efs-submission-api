package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private static final String SUB_ID = "0000000000";

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
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(SUB_ID).build();

        when(repository.findById(SUB_ID)).thenReturn(Optional.of(expected));

        testService.getTemplate(SUB_ID);

        verify(repository).findById(SUB_ID);
    }

    @DisplayName("GIVEN a template ID to retrieve a payment template that does not exist"
                 + "WHEN fetch template by ID using the service "
                 + "THEN the result is empty")
    @Test
    void getTemplateWhenNotFound() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(SUB_ID).build();

        when(repository.findById(SUB_ID)).thenReturn(Optional.empty());

        testService.getTemplate(SUB_ID);

        verify(repository).findById(SUB_ID);
    }

    @DisplayName("GIVEN a payment template to store "
                 + "WHEN put template using the service "
                 + "THEN the object is stored")
    @Test
    void putTemplate() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(SUB_ID).build();
        testService.putTemplate(expected);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId(), is(SUB_ID));
    }
}