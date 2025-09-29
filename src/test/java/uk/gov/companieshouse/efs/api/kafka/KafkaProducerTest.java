package uk.gov.companieshouse.efs.api.kafka;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {
    private static final String EXPECTED_CONFIG_ERROR_MESSAGE =
            "Broker addresses for kafka broker missing, check if environment variable KAFKA_BROKER_ADDR is configured. " +
                    "[Hint: The property 'kafka.broker.addresses' uses the value of this environment variable in live " +
                    "environments and that of 'spring.embedded.kafka.brokers' property in test.]";

    @InjectMocks
    private TestKafkaProducer kafkaProducerUnderTest;

    @Mock
    private CHKafkaProducer chKafkaProducer;

    @Mock
    private ProducerConfig producerConfig;

    /**
     * Extends {@link KafkaProducer} to provide a concrete implementation for testing.
     */
    private static class TestKafkaProducer extends KafkaProducer {}

    /**
     * Extends {@link KafkaProducer} to provide a concrete implementation for testing that allows us to stub out
     * unwanted behaviour and verify the behaviour of interest.
     */
    private class TestKafkaProducer2 extends KafkaProducer {
        private boolean modifyProducerConfigCalled;
        private boolean createProducerConfigCalled;
        private boolean createChKafkaProducerCalled;

        public boolean isModifyProducerConfigCalled() {
            return modifyProducerConfigCalled;
        }

        public boolean isCreateProducerConfigCalled() {
            return createProducerConfigCalled;
        }

        public boolean isCreateChKafkaProducerCalled() {
            return createChKafkaProducerCalled;
        }

        @Override
        protected void modifyProducerConfig(ProducerConfig producerConfig) {
            modifyProducerConfigCalled = true;
        }

        @Override
        protected ProducerConfig createProducerConfig() {
            createProducerConfigCalled = true;
            return producerConfig;
        }

        @Override
        protected CHKafkaProducer createChKafkaProducer(final ProducerConfig config) {
            createChKafkaProducerCalled = true;
            return chKafkaProducer;
        }
    }

    @Test
    @DisplayName("createProducerConfig() sets broker addresses correctly when valid addresses are provided")
    void createProducerConfigSetsBrokerAddressesCorrectly() {
        // Given
        ReflectionTestUtils.setField(kafkaProducerUnderTest, "brokerAddresses", "broker1,broker2");

        // When
        ProducerConfig config = kafkaProducerUnderTest.createProducerConfig();

        // Then
        assertThat(config.getBrokerAddresses(), is(new String[]{"broker1", "broker2"}));
    }

    @Test
    @DisplayName("createProducerConfig() throws ProducerConfigException when broker addresses are empty")
    void createProducerConfigThrowsExceptionWhenBrokerAddressesEmpty() {
        // Given
        ReflectionTestUtils.setField(kafkaProducerUnderTest, "brokerAddresses", "");

        // When
        ProducerConfigException exception = Assertions.assertThrows(ProducerConfigException.class, kafkaProducerUnderTest::createProducerConfig);

        // Then
        assertThat(exception.getMessage(), is(EXPECTED_CONFIG_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("createProducerConfig() throws ProducerConfigException when broker addresses are null")
    void createProducerConfigThrowsExceptionWhenBrokerAddressesNull() {
        // Given
        ReflectionTestUtils.setField(kafkaProducerUnderTest, "brokerAddresses", null);

        // When
        ProducerConfigException exception = Assertions.assertThrows(ProducerConfigException.class, kafkaProducerUnderTest::createProducerConfig);

        // Then
        assertThat(exception.getMessage(), is(EXPECTED_CONFIG_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("createChKafkaProducer() creates a CHKafkaProducer with the provided config")
    void createChKafkaProducerCreatesProducerWithProvidedConfig() {
        // Given
        final TestKafkaProducer2 kafkaProducer2UnderTest = new TestKafkaProducer2();

        // When
        CHKafkaProducer result = kafkaProducer2UnderTest.createChKafkaProducer(producerConfig);

        // Then
        assertThat(result, is(notNullValue()));
    }

    @Test
    @DisplayName("afterPropertiesSet() throws a ProducerConfigException if no spring.kafka.bootstrap-servers value configured")
    void afterPropertiesSetThrowsExceptionIfNoBrokersConfigured() {

        // When
        ProducerConfigException exception = Assertions.assertThrows(ProducerConfigException.class, () ->
                kafkaProducerUnderTest.afterPropertiesSet());
        // Then
        final String actualMessage = exception.getMessage();
        assertThat(actualMessage, is(EXPECTED_CONFIG_ERROR_MESSAGE));

    }

    @Test
    @DisplayName("afterPropertiesSet() calls template methods")
    void afterPropertiesSetCallsTemplateMethods() {

        // Given
        final TestKafkaProducer2 kafkaProducer2UnderTest = new TestKafkaProducer2();

        // When
        kafkaProducer2UnderTest.afterPropertiesSet();

        // Then
        assertThat(kafkaProducer2UnderTest.isModifyProducerConfigCalled(), is(true));
        assertThat(kafkaProducer2UnderTest.isCreateProducerConfigCalled(), is(true));
        assertThat(kafkaProducer2UnderTest.isCreateChKafkaProducerCalled(), is(true));

    }

    @Test
    @DisplayName("afterPropertiesSet() sets the producer's kafka producer member")
    void afterPropertiesSetSetsProducerMember() {

        // Given
        final TestKafkaProducer2 kafkaProducer2UnderTest = new TestKafkaProducer2();

        // When
        kafkaProducer2UnderTest.afterPropertiesSet();

        // Then
        assertThat(kafkaProducer2UnderTest.getChKafkaProducer(), is(notNullValue()));

    }

    @Test
    @DisplayName("afterPropertiesSet() sets producer config properties")
    void afterPropertiesSetSetsProducerConfigProperties() {

        // Given
        final TestKafkaProducer2 kafkaProducer2UnderTest = new TestKafkaProducer2();

        // When
        kafkaProducer2UnderTest.afterPropertiesSet();

        // Then
        verify(producerConfig).setRoundRobinPartitioner(true);
        verify(producerConfig).setAcks(Acks.WAIT_FOR_ALL);
        verify(producerConfig).setRetries(10);

    }
}