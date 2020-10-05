package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentReferenceMapperTest {

    private PaymentReferenceMapper paymentReferenceMapper;

    @Mock
    private PaymentReferenceApi paymentReferenceApi;

    @BeforeEach
    void setUp(){
        this.paymentReferenceMapper = new PaymentReferenceMapper();
    }

    @Test
    void testPaymentReferenceMapperReturnsStringRepresentationOfPaymentReference() {
        //given
        when(paymentReferenceApi.getPaymentReference()).thenReturn("1234567890");

        //when
        String actual = paymentReferenceMapper.map(paymentReferenceApi);

        //then
        assertEquals("1234567890", actual);
    }
}
