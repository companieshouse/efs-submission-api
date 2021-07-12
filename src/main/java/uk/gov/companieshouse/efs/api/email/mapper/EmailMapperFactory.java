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
    private DelayedSH19SameDaySubmissionSupportEmailMapper
        delayedSH19SameDaySubmissionSupportEmailMapper;
    private DelayedSubmissionBusinessEmailMapper delayedSubmissionBusinessEmailMapper;
    private DelayedSH19SameDaySubmissionBusinessEmailMapper
        delayedSH19SameDaySubmissionBusinessEmailMapper;
    private PaymentReportEmailMapper paymentReportEmailMapper;

    private EmailMapperFactory() {
        // intentionally blank
    }

    public static Builder newBuilder() {
        return new Builder();
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

    public DelayedSH19SameDaySubmissionBusinessEmailMapper getDelayedSH19SameDaySubmissionBusinessEmailMapper() {
        return delayedSH19SameDaySubmissionBusinessEmailMapper;
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
            buildSteps = new ArrayList<>();
        }

        /**
         * Sets the {@code confirmationEmailMapper} and returns a reference to this Builder so 
         * that the methods can be chained together.
         *
         * @param confirmationEmailMapper the {@code confirmationEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withConfirmationEmailMapper(final ExternalNotificationEmailMapper confirmationEmailMapper) {
            buildSteps.add(m -> m.confirmationEmailMapper = confirmationEmailMapper);
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
            buildSteps.add(m -> m.paymentFailedEmailMapper = paymentFailedEmailMapper);
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
            buildSteps.add(m -> m.acceptEmailMapper = acceptEmailMapper);
            return this;
        }

        /**
         * Sets the {@code rejectEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param rejectEmailMapper the {@code rejectEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withRejectEmailMapper(final ExternalRejectEmailMapper rejectEmailMapper) {
            buildSteps.add(m -> m.rejectEmailMapper = rejectEmailMapper);
            return this;
        }

        /**
         * Sets the {@code internalAvFailedEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param internalAvFailedEmailMapper the {@code internalAvFailedEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withInternalAvFailedEmailMapper(final InternalAvFailedEmailMapper internalAvFailedEmailMapper) {
            buildSteps.add(m -> m.internalAvFailedEmailMapper = internalAvFailedEmailMapper);
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
            buildSteps.add(
                m -> m.internalFailedConversionEmailMapper = internalFailedConversionEmailMapper);
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
            buildSteps.add(m -> m.internalSubmissionEmailMapper = internalSubmissionEmailMapper);
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
            buildSteps.add(
                m -> m.delayedSubmissionSupportEmailMapper = delayedSubmissionSupportEmailMapper);
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
            buildSteps.add(m -> m.delayedSH19SameDaySubmissionSupportEmailMapper =
                delayedSH19SameDaySubmissionSupportEmailMapper);
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
            buildSteps.add(
                m -> m.delayedSubmissionBusinessEmailMapper = delayedSubmissionBusinessEmailMapper);
            return this;
        }

        /**
         * Sets the {@code delayedSH19SubmissionBusinessEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param delayedSH19SameDaySubmissionBusinessEmailMapper the {@code delayedSubmissionSupportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withDelayedSH19SameDaySubmissionBusinessEmailMapper(
            final DelayedSH19SameDaySubmissionBusinessEmailMapper delayedSH19SameDaySubmissionBusinessEmailMapper) {
            buildSteps.add(m -> m.delayedSH19SameDaySubmissionBusinessEmailMapper =
                delayedSH19SameDaySubmissionBusinessEmailMapper);
            return this;
        }


        /**
         * Sets the {@code paymentReportEmailMapper} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param paymentReportEmailMapper the {@code paymentReportEmailMapper} to set
         * @return a reference to this Builder
         */
        public Builder withPaymentReportEmailMapper(final PaymentReportEmailMapper paymentReportEmailMapper) {
            buildSteps.add(m -> m.paymentReportEmailMapper = paymentReportEmailMapper);
            return this;
        }

        /**
         * Returns a {@code EmailMapperFactory} built from the parameters previously set.
         *
         * @return a {@code EmailMapperFactory} built with parameters of this {@code EmailMapperFactory.Builder}
         */
        public EmailMapperFactory build() {
            EmailMapperFactory factory = new EmailMapperFactory();

            buildSteps.forEach(step -> step.accept(factory));
            validate(factory);

            return factory;
        }

        private static void validate(EmailMapperFactory factory) {
            checkNotNull(factory.getAcceptEmailMapper(), "'acceptEmailMapper' must not be null");
            checkNotNull(factory.getConfirmationEmailMapper(),
                "'confirmationEmailMapper' must not be null");
            checkNotNull(factory.getPaymentFailedEmailMapper(),
                "'paymentFailedEmailMapper' must not be null");
            checkNotNull(factory.getDelayedSubmissionBusinessEmailMapper(),
                "'delayedSubmissionBusinessEmailMapper' must not be null");
            checkNotNull(factory.getDelayedSH19SameDaySubmissionBusinessEmailMapper(),
                "'this.delayedSH19SameDaySubmissionBusinessEmailMapper' must not be null");
            checkNotNull(factory.getDelayedSubmissionSupportEmailMapper(),
                "'delayedSubmissionSupportEmailMapper' must not be null");
            checkNotNull(factory.getDelayedSH19SameDaySubmissionSupportEmailMapper(),
                "'this.delayedSH19SameDaySubmissionSupportEmailMapper' must not be null");
            checkNotNull(factory.getInternalAvFailedEmailMapper(),
                "'internalAVFailedEmailMapper' must not be null");
            checkNotNull(factory.getInternalFailedConversionEmailMapper(),
                "'internalFailedConversionEmailMapper' must not be null");
            checkNotNull(factory.getInternalSubmissionEmailMapper(),
                "'internalSubmissionEmailMapper' must not be null");
            checkNotNull(factory.getPaymentReportEmailMapper(),
                "'paymentReportEmailMapper' must not be null");
            checkNotNull(factory.getPaymentReportEmailMapper(), "'rejectEmailMapper' must not be null");

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
        final EmailMapperFactory factory = (EmailMapperFactory) o;
        return Objects.equals(getConfirmationEmailMapper(), factory.getConfirmationEmailMapper())
            && Objects.equals(getPaymentFailedEmailMapper(), factory.getPaymentFailedEmailMapper())
            && Objects.equals(getAcceptEmailMapper(), factory.getAcceptEmailMapper())
            && Objects.equals(getRejectEmailMapper(), factory.getRejectEmailMapper())
            && Objects.equals(getInternalAvFailedEmailMapper(),
            factory.getInternalAvFailedEmailMapper()) && Objects.equals(
            getInternalFailedConversionEmailMapper(),
            factory.getInternalFailedConversionEmailMapper()) && Objects.equals(
            getInternalSubmissionEmailMapper(), factory.getInternalSubmissionEmailMapper())
            && Objects.equals(getDelayedSubmissionSupportEmailMapper(),
            factory.getDelayedSubmissionSupportEmailMapper()) && Objects.equals(
            getDelayedSH19SameDaySubmissionSupportEmailMapper(),
            factory.getDelayedSH19SameDaySubmissionSupportEmailMapper()) && Objects.equals(
            getDelayedSubmissionBusinessEmailMapper(),
            factory.getDelayedSubmissionBusinessEmailMapper()) && Objects.equals(
            getDelayedSH19SameDaySubmissionBusinessEmailMapper(),
            factory.getDelayedSH19SameDaySubmissionBusinessEmailMapper()) && Objects.equals(
            getPaymentReportEmailMapper(), factory.getPaymentReportEmailMapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConfirmationEmailMapper(), getPaymentFailedEmailMapper(),
            getAcceptEmailMapper(), getRejectEmailMapper(), getInternalAvFailedEmailMapper(),
            getInternalFailedConversionEmailMapper(), getInternalSubmissionEmailMapper(),
            getDelayedSubmissionSupportEmailMapper(),
            getDelayedSH19SameDaySubmissionSupportEmailMapper(),
            getDelayedSubmissionBusinessEmailMapper(),
            getDelayedSH19SameDaySubmissionBusinessEmailMapper(), getPaymentReportEmailMapper());
    }
}
