package uk.gov.companieshouse.efs.api.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Embeddable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.Field;

@Embeddable
public final class PaymentTemplateId implements Serializable {

    private static final long serialVersionUID = -3666317728117130710L;

    private String fee;
    @Field("active_from")
    @JsonProperty("active_from")
    private LocalDateTime activeFrom;

    public PaymentTemplateId() {
    }

    public PaymentTemplateId(final String fee, final LocalDateTime activeFrom) {
        this.fee = fee;
        this.activeFrom = activeFrom;
    }

    public String getFee() {
        return fee;
    }

    public LocalDateTime getActiveFrom() {
        return activeFrom;
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
        return Objects.equal(getFee(), that.getFee()) && Objects.equal(getActiveFrom(),
            that.getActiveFrom());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFee(), getActiveFrom());
    }

    @Override
    public java.lang.String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("fee", fee)
            .append("activeFrom", getActiveFrom())
                .toString();
    }
}
