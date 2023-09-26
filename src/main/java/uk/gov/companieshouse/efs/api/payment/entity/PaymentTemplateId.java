package uk.gov.companieshouse.efs.api.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Embeddable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.Field;

@Embeddable
public final class PaymentTemplateId implements Serializable {

    private static final long serialVersionUID = -3666317728117130710L;

    private String fee;
    @Field("start_timestamp")
    @JsonProperty("start_timestamp")
    private Instant startTimestamp;

    public PaymentTemplateId() {
    }

    public PaymentTemplateId(final String fee, final Instant startTimestamp) {
        this.fee = fee;
        this.startTimestamp = startTimestamp;
    }

    public String getFee() {
        return fee;
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
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
        return Objects.equal(getFee(), that.getFee()) && Objects.equal(getStartTimestamp(),
                that.getStartTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFee(), getStartTimestamp());
    }

    @Override
    public java.lang.String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("fee", fee)
                .append("startTimestamp", startTimestamp)
                .toString();
    }
}
