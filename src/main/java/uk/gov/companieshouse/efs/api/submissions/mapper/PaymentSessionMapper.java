package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.efs.api.submissions.model.PaymentSession;

@Component
public class PaymentSessionMapper {
    public PaymentSession map(SessionApi paymentSessionApi) {
        return new PaymentSession(paymentSessionApi.getSessionId(), paymentSessionApi.getSessionState());
    }
}
