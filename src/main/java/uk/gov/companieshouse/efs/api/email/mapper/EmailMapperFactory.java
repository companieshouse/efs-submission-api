package uk.gov.companieshouse.efs.api.email.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EmailMapperFactory {
    private ExternalNotificationEmailMapper confirmationEmailMapper;
    private ExternalNotificationEmailMapper paymentFailedEmailMapper;
    private ExternalAcceptEmailMapper acceptEmailMapper;
    private ExternalRejectEmailMapper rejectEmailMapper;
    private InternalAvFailedEmailMapper internalAvFailedEmailMapper;
    private InternalFailedConversionEmailMapper internalFailedConversionEmailMapper;
    private InternalSubmissionEmailMapper internalSubmissionEmailMapper;
    private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;
    private DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper;
    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;
    private PaymentReportEmailMapper paymentReportEmailMapper;

    private EmailMapperFactory() {
        // intentionally blank
    }


    public static Builder newBuilder() {
        return new EmailMapperFactory.Builder();
    }

    public ExternalNotificationEmailMapper getConfirmationEmailMapper() {
        return confirmationEmailMapper;
    }

    public ExternalNotificationEmailMapper getPaymentFailedEmailMapper() {
        return paymentFailedEmailMapper;
    }

    public ExternalAcceptEmailMapper getAcceptEmailMapper() {
        return acceptEmailMapper;
    }

    public ExternalRejectEmailMapper getRejectEmailMapper() {
        return rejectEmailMapper;
    }

    public InternalAvFailedEmailMapper getInternalAvFailedEmailMapper() {
        return internalAvFailedEmailMapper;
    }

    public InternalFailedConversionEmailMapper getInternalFailedConversionEmailMapper() {
        return internalFailedConversionEmailMapper;
    }

    public InternalSubmissionEmailMapper getInternalSubmissionEmailMapper() {
        return internalSubmissionEmailMapper;
    }

    public DelayedSubmissionSupportEmailMapper getDelayedSubmissionSupportEmailMapper() {
        return delayedSubmissionSupportEmailMapper;
    }

    public DelayedSH19SameDaySubmissionSupportEmailMapper getDelayedSH19SameDaySubmissionSupportEmailMapper() {
        return delayedSH19SameDaySubmissionSupportEmailMapper;
    }

    public DelayedSubmissionBusinessEmailMapper getDelayedSubmissionBusinessEmailMapper() {
        return delayedSubmissionBusinessEmailMapper;
    }

    public PaymentReportEmailMapper getPaymentReportEmailMapper() {
        return paymentReportEmailMapper;
    }

    /**
     * {@code EmailMapperFactory} builder static inner class.
     */
    public static final class Builder {

        private final List<Consumer<EmailMapperFactory>> buildSteps;

        private Builder() {
            this.buildSteps = new ArrayList<>();
        }

        
        /**
         * Sets the {@code confirmationEmailMapper} and returns a reference to this Builder so 
         * that the methods can be chained together.
         *
         * @param confirmationEmailMapper the {@code confirmationEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withConfirmationEmailMapper(final ExternalNotificationEmailMapper confirmationEmailMapper) {
            buildSteps.add(data -> data.confirmationEmailMapper = confirmationEmailMapper);
            return this;
        }

        /**
         * Sets the {@code paymentFailedEmailMapper} and returns a reference to this Builder so 
         * that the methods can be chained together.
         *
         * @param paymentFailedEmailMapper the {@code paymentFailedEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withPaymentFailedEmailMapper(
            final ExternalNotificationEmailMapper paymentFailedEmailMapper) {
            buildSteps.add(data -> data.paymentFailedEmailMapper = paymentFailedEmailMapper);
            return this;
        }

        /**
         * Sets the {@code acceptEmailMapper} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param acceptEmailMapper the {@code acceptEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withAcceptEmailMapper(final ExternalAcceptEmailMapper acceptEmailMapper) {
            buildSteps.add(data -> data.acceptEmailMapper = acceptEmailMapper);
            return this;
        }

        /**
         * Sets the {@code rejectEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param rejectEmailMapper the {@code rejectEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withRejectEmailMapper(final ExternalRejectEmailMapper rejectEmailMapper) {
            buildSteps.add(data -> data.rejectEmailMapper = rejectEmailMapper);
            return this;
        }

        /**
         * Sets the {@code internalAvFailedEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param internalAvFailedEmailMapper the {@code internalAvFailedEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withInternalAvFailedEmailMapper(final InternalAvFailedEmailMapper internalAvFailedEmailMapper) {
            buildSteps.add(data -> data.internalAvFailedEmailMapper = internalAvFailedEmailMapper);
            return this;
        }

        /**
         * Sets the {@code internalFailedConversionEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param internalFailedConversionEmailMapper the {@code internalFailedConversionEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withInternalFailedConversionEmailMapper(
            final InternalFailedConversionEmailMapper internalFailedConversionEmailMapper) {
            buildSteps.add(data -> data.internalFailedConversionEmailMapper = internalFailedConversionEmailMapper);
            return this;
        }

        /**
         * Sets the {@code internalSubmissionEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param internalSubmissionEmailMapper the {@code internalSubmissionEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withInternalSubmissionEmailMapper(
            final InternalSubmissionEmailMapper internalSubmissionEmailMapper) {
            buildSteps.add(data -> data.internalSubmissionEmailMapper = internalSubmissionEmailMapper);
            return this;
        }

        /**
         * Sets the {@code delayedSubmissionSupportEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param delayedSubmissionSupportEmailMapper the {@code delayedSubmissionSupportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withDelayedSubmissionSupportEmailMapper(
            final DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper) {
            buildSteps.add(data -> data.delayedSubmissionSupportEmailMapper = delayedSubmissionSupportEmailMapper);
            return this;
        }

        /**
         * Sets the {@code delayedSH19SubmissionSupportEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param delayedSH19SameDaySubmissionSupportEmailMapper the {@code delayedSubmissionSupportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withDelayedSH19SameDaySubmissionSupportEmailMapper(
            final DelayedSH19SameDaySubmissionSupportEmailMapper delayedSH19SameDaySubmissionSupportEmailMapper) {
            buildSteps.add(data -> data.delayedSH19SameDaySubmissionSupportEmailMapper = delayedSH19SameDaySubmissionSupportEmailMapper);
            return this;
        }

        /**
         * Sets the {@code delayedSubmissionBusinessEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param delayedSubmissionBusinessEmailMapper the {@code delayedSubmissionBusinessEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withDelayedSubmissionBusinessEmailMapper(
            final DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper) {
            buildSteps.add(data -> data.delayedSubmissionBusinessEmailMapper = delayedSubmissionBusinessEmailMapper);
            return this;
        }

        /**
         * Sets the {@code paymentReportEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param paymentReportEmailMapper the {@code paymentReportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withPaymentReportEmailMapper(final PaymentReportEmailMapper paymentReportEmailMapper) {
            buildSteps.add(data -> data.paymentReportEmailMapper = paymentReportEmailMapper);
            return this;
        }

        /**
         * Returns a {@code EmailMapperFactory} built from the parameters previously set.
         *
         * @return a {@code EmailMapperFactory} built with parameters of this {@code EmailMapperFactory.Builder}
         */
        public EmailMapperFactory build() {
            EmailMapperFactory data = new EmailMapperFactory();

            buildSteps.forEach(step -> step.accept(data));

            validate(data);
            return data;
        }

        public void validate(EmailMapperFactory data) {
            checkNotNull(data.getAcceptEmailMapper(), "acceptEmailMapper");
            checkNotNull(data.getConfirmationEmailMapper(),
                    "confirmationEmailMapper");
            checkNotNull(data.getPaymentFailedEmailMapper(),
                    "paymentFailedEmailMapper");
            checkNotNull(data.getDelayedSubmissionBusinessEmailMapper(),
                    "delayedSubmissionBusinessEmailMapper");
            checkNotNull(data.getDelayedSubmissionSupportEmailMapper(), "delayedSubmissionSupportEmailMapper");
            checkNotNull(data.getDelayedSH19SameDaySubmissionSupportEmailMapper(),
                    "delayedSH19SameDaySubmissionSupportEmailMapper");
            checkNotNull(data.getInternalAvFailedEmailMapper(), "internalAVFailedEmailMapper");
            checkNotNull(data.getInternalFailedConversionEmailMapper(), "internalFailedConversionEmailMapper");
            checkNotNull(data.getInternalSubmissionEmailMapper(), "internalSubmissionEmailMapper");
            checkNotNull(data.getPaymentReportEmailMapper(), "paymentReportEmailMapper");
            checkNotNull(data.getRejectEmailMapper(), "rejectEmailMapper");
        }

        private static <T> T checkNotNull(T t, String fieldName) {
            if (t == null) {
                String msg = String.format("'%s' must not be null", fieldName);
                throw new IllegalArgumentException(msg);
            }
            return t;
        }


    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmailMapperFactory that = (EmailMapperFactory) o;
        return Objects.equals(getConfirmationEmailMapper(), that.getConfirmationEmailMapper())
            && Objects.equals(getPaymentFailedEmailMapper(), that.getPaymentFailedEmailMapper())
            && Objects.equals(getAcceptEmailMapper(), that.getAcceptEmailMapper())
            && Objects.equals(getRejectEmailMapper(), that.getRejectEmailMapper())
            && Objects.equals(getInternalAvFailedEmailMapper(),
            that.getInternalAvFailedEmailMapper()) && Objects.equals(
            getInternalFailedConversionEmailMapper(), that.getInternalFailedConversionEmailMapper())
            && Objects.equals(getInternalSubmissionEmailMapper(),
            that.getInternalSubmissionEmailMapper()) && Objects.equals(
            getDelayedSubmissionSupportEmailMapper(), that.getDelayedSubmissionSupportEmailMapper())
            && Objects.equals(getDelayedSH19SameDaySubmissionSupportEmailMapper(),
            that.getDelayedSH19SameDaySubmissionSupportEmailMapper()) && Objects.equals(
            getDelayedSubmissionBusinessEmailMapper(),
            that.getDelayedSubmissionBusinessEmailMapper()) && Objects.equals(
            getPaymentReportEmailMapper(), that.getPaymentReportEmailMapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConfirmationEmailMapper(), getPaymentFailedEmailMapper(),
            getAcceptEmailMapper(), getRejectEmailMapper(), getInternalAvFailedEmailMapper(),
            getInternalFailedConversionEmailMapper(), getInternalSubmissionEmailMapper(),
            getDelayedSubmissionSupportEmailMapper(),
            getDelayedSH19SameDaySubmissionSupportEmailMapper(),
            getDelayedSubmissionBusinessEmailMapper(), getPaymentReportEmailMapper());
    }
}
