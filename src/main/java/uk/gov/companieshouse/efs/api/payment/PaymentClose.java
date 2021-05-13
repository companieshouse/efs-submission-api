package uk.gov.companieshouse.efs.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDateTime;

public class PaymentClose {
    @JsonProperty("paid_at")
    private LocalDateTime paidAt;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private String status;

    @JsonCreator()
    public PaymentClose(@JsonProperty("payment_reference") final String paymentReference,
        @JsonProperty("status") final PaymentClose.Status status,
        @JsonProperty("paid_at") final LocalDateTime paidAt) {
        this.paidAt = paidAt;
        this.paymentReference = paymentReference;
        this.status = status.toString();
    }

    public PaymentClose(final String paymentReference, final PaymentClose.Status status) {
        this(paymentReference, status, null);
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isPaid() {
        return Status.PAID.equals(Status.fromValue(status));
    }

    public boolean isFailed() {
        return Status.FAILED.equals(Status.fromValue(status));
    }

    public enum Status {
        PAID("paid"), FAILED("failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Returns the status or null if no match found
         *
         * @param text the status value
         * @return the status or null if no match found
         */
        @JsonCreator
        public static Status fromValue(String text) {
            for (Status b : Status.values()) {
                if (String.valueOf(b.value)
                    .equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }
}
