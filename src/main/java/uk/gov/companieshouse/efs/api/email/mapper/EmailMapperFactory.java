package uk.gov.companieshouse.efs.api.email.mapper;

import java.util.Objects;

public class EmailMapperFactory {
    private ExternalNotificationEmailMapper confirmationEmailMapper;
    private ExternalNotificationEmailMapper paymentFailedEmailMapper;
    private ExternalAcceptEmailMapper acceptEmailMapper;
    private ExternalRejectEmailMapper rejectEmailMapper;
    private InternalAvFailedEmailMapper internalAvFailedEmailMapper;
    private InternalFailedConversionEmailMapper internalFailedConversionEmailMapper;
    private InternalSubmissionEmailMapper internalSubmissionEmailMapper;
    private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;
    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;
    private PaymentReportEmailMapper paymentReportEmailMapper;

    private EmailMapperFactory() {
        // intentionally blank
    }

    private EmailMapperFactory(final Builder builder) {
        confirmationEmailMapper = builder.confirmationEmailMapper;
        paymentFailedEmailMapper = builder.paymentFailedEmailMapper;
        acceptEmailMapper = builder.acceptEmailMapper;
        rejectEmailMapper = builder.rejectEmailMapper;
        internalAvFailedEmailMapper = builder.internalAvFailedEmailMapper;
        internalFailedConversionEmailMapper = builder.internalFailedConversionEmailMapper;
        internalSubmissionEmailMapper = builder.internalSubmissionEmailMapper;
        delayedSubmissionSupportEmailMapper = builder.delayedSubmissionSupportEmailMapper;
        delayedSubmissionBusinessEmailMapper = builder.delayedSubmissionBusinessEmailMapper;
        paymentReportEmailMapper = builder.paymentReportEmailMapper;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final EmailMapperFactory copy) {
        Builder builder = new Builder();
        builder.confirmationEmailMapper = copy.getConfirmationEmailMapper();
        builder.paymentFailedEmailMapper = copy.getPaymentFailedEmailMapper();
        builder.acceptEmailMapper = copy.getAcceptEmailMapper();
        builder.rejectEmailMapper = copy.getRejectEmailMapper();
        builder.internalAvFailedEmailMapper = copy.getInternalAvFailedEmailMapper();
        builder.internalFailedConversionEmailMapper = copy.getInternalFailedConversionEmailMapper();
        builder.internalSubmissionEmailMapper = copy.getInternalSubmissionEmailMapper();
        builder.delayedSubmissionSupportEmailMapper = copy.getDelayedSubmissionSupportEmailMapper();
        builder.delayedSubmissionBusinessEmailMapper =
            copy.getDelayedSubmissionBusinessEmailMapper();
        builder.paymentReportEmailMapper = copy.getPaymentReportEmailMapper();
        return builder;
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
        private ExternalNotificationEmailMapper confirmationEmailMapper;
        private ExternalNotificationEmailMapper paymentFailedEmailMapper;
        private ExternalAcceptEmailMapper acceptEmailMapper;
        private ExternalRejectEmailMapper rejectEmailMapper;
        private InternalAvFailedEmailMapper internalAvFailedEmailMapper;
        private InternalFailedConversionEmailMapper internalFailedConversionEmailMapper;
        private InternalSubmissionEmailMapper internalSubmissionEmailMapper;
        private DelayedSubmissionSupportEmailMapper delayedSubmissionSupportEmailMapper;
        private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;
        private PaymentReportEmailMapper paymentReportEmailMapper;

        private Builder() {
        }

        /**
         * Sets the {@code confirmationEmailMapper} and returns a reference to this Builder so 
         * that the methods can be chained together.
         *
         * @param confirmationEmailMapper the {@code confirmationEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withConfirmationEmailMapper(final ExternalNotificationEmailMapper confirmationEmailMapper) {
            this.confirmationEmailMapper = confirmationEmailMapper;
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
            this.paymentFailedEmailMapper = paymentFailedEmailMapper;
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
            this.acceptEmailMapper = acceptEmailMapper;
            return this;
        }

        /**
         * Sets the {@code rejectEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param rejectEmailMapper the {@code rejectEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withRejectEmailMapper(final ExternalRejectEmailMapper rejectEmailMapper) {
            this.rejectEmailMapper = rejectEmailMapper;
            return this;
        }

        /**
         * Sets the {@code internalAvFailedEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param internalAvFailedEmailMapper the {@code internalAvFailedEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withInternalAvFailedEmailMapper(final InternalAvFailedEmailMapper internalAvFailedEmailMapper) {
            this.internalAvFailedEmailMapper = internalAvFailedEmailMapper;
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
            this.internalFailedConversionEmailMapper = internalFailedConversionEmailMapper;
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
            this.internalSubmissionEmailMapper = internalSubmissionEmailMapper;
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
            this.delayedSubmissionSupportEmailMapper = delayedSubmissionSupportEmailMapper;
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
            this.delayedSubmissionBusinessEmailMapper = delayedSubmissionBusinessEmailMapper;
            return this;
        }

        /**
         * Sets the {@code paymentReportEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param paymentReportEmailMapper the {@code paymentReportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withPaymentReportEmailMapper(final PaymentReportEmailMapper paymentReportEmailMapper) {
            this.paymentReportEmailMapper = paymentReportEmailMapper;
            return this;
        }

        /**
         * Returns a {@code EmailMapperFactory} built from the parameters previously set.
         *
         * @return a {@code EmailMapperFactory} built with parameters of this {@code EmailMapperFactory.Builder}
         */
        public EmailMapperFactory build() {
            checkNotNull(this.acceptEmailMapper, "'acceptEmailMapper' must not be null");
            checkNotNull(this.confirmationEmailMapper,
                "'confirmationEmailMapper' must not be null");
            checkNotNull(this.paymentFailedEmailMapper,
                "'paymentFailedEmailMapper' must not be null");
            checkNotNull(this.delayedSubmissionBusinessEmailMapper,
                "'delayedSubmissionBusinessEmailMapper' must not be null");
            checkNotNull(this.delayedSubmissionSupportEmailMapper, "'delayedSubmissionSupportEmailMapper' must not be null");
            checkNotNull(this.internalAvFailedEmailMapper, "'internalAVFailedEmailMapper' must not be null");
            checkNotNull(this.internalFailedConversionEmailMapper, "'internalFailedConversionEmailMapper' must not be null");
            checkNotNull(this.internalSubmissionEmailMapper, "'internalSubmissionEmailMapper' must not be null");
            checkNotNull(this.paymentReportEmailMapper, "'paymentReportEmailMapper' must not be null");
            checkNotNull(this.rejectEmailMapper, "'rejectEmailMapper' must not be null");

            return new EmailMapperFactory(this);
        }

        private static <T> T checkNotNull(T t, String msg) {
            if (t == null) {
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
            && Objects.equals(getDelayedSubmissionBusinessEmailMapper(),
            that.getDelayedSubmissionBusinessEmailMapper()) && Objects.equals(
            getPaymentReportEmailMapper(), that.getPaymentReportEmailMapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConfirmationEmailMapper(), getPaymentFailedEmailMapper(),
            getAcceptEmailMapper(), getRejectEmailMapper(), getInternalAvFailedEmailMapper(),
            getInternalFailedConversionEmailMapper(), getInternalSubmissionEmailMapper(),
            getDelayedSubmissionSupportEmailMapper(), getDelayedSubmissionBusinessEmailMapper(),
            getPaymentReportEmailMapper());
    }
}
