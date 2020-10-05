package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;

@Component
public class PaymentReferenceMapper {
    public String map(PaymentReferenceApi paymentReferenceApi) {
        return paymentReferenceApi.getPaymentReference();
    }
}
