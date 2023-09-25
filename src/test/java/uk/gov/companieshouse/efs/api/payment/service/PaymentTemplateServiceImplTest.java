package uk.gov.companieshouse.efs.api.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateTest.TEMPLATE_ID;

import java.util.ArrayList;
import java.util.List;
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

        //FIXME when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.of(expected));

        //FIXME testService.getTemplate(TEMPLATE_ID);

        //FIXME verify(repository).findById(TEMPLATE_ID);
    }

    @Test
    @DisplayName("GIVEN a template ID to retrieve a payment template that contains > 1 item"
            + "WHEN fetch template by ID using the service "
            + "THEN the result is the found object")
    void getTemplateItemsById() {
        PaymentTemplate.Item item1 = PaymentTemplate.Item.newBuilder()
                .withAmount("17")
                .withStartTimestampUtc("2019-01-08T00:00:00.000Z")
                .withEndTimestampUtc("2019-01-08T00:00:00.000Z")
                .build();
        PaymentTemplate.Item item2 = PaymentTemplate.Item.newBuilder()
                .withAmount("57")
                .withStartTimestampUtc("2024-01-08T00:00:00.000Z")
                .withEndTimestampUtc("9999-12-31T00:00:00.000Z")
                .build();
        List items = new ArrayList<PaymentTemplate.Item>();
        items.add(item1);
        items.add(item2);

        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID).withItems(items)
                .build();


    }

    @DisplayName("GIVEN a template ID to retrieve a payment template that does not exist"
                 + "WHEN fetch template by ID using the service "
                 + "THEN the result is empty")
    @Test
    void getTemplateWhenNotFound() {
        PaymentTemplate expected = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
                .build();

        //FIXME when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());

        //FIXME testService.getTemplate(TEMPLATE_ID);

        //FIXME verify(repository).findById(TEMPLATE_ID);
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