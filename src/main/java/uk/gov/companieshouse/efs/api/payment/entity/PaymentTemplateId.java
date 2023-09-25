package uk.gov.companieshouse.efs.api.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Embeddable;
import org.springframework.data.mongodb.core.mapping.Field;

@Embeddable
public class PaymentTemplateId implements Serializable {

    private static final long serialVersionUID = -3666317728117130710L;

    private String fee;
    @Field("start_timestamp_utc")
    @JsonProperty("start_timestamp_utc")
    private Instant startTimestampUtc;

    public PaymentTemplateId() {
    }

    public PaymentTemplateId(final String fee, final Instant startTimestampUtc) {
        this.fee = fee;
        this.startTimestampUtc = startTimestampUtc;
    }

    public String getFee() {
        return fee;
    }

    public Instant getStartTimestampUtc() {
        return startTimestampUtc;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentTemplateId)) {
            return false;
        }
        final PaymentTemplateId that = (PaymentTemplateId) o;
        return Objects.equals(getFee(), that.getFee()) && Objects.equals(getStartTimestampUtc(),
                that.getStartTimestampUtc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFee(), getStartTimestampUtc());
    }
}
