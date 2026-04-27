package uk.gov.companieshouse.efs.api.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.factory.KafkaProducerFactory;

@ExtendWith(MockitoExtension.class)
class CHKafkaProducerTest {

    private static final int TEST_RETRIES = 5;
    private static final String TEST_BROKER = "test-broker";
    private static final String KEY = "test-key";
    private static final Long OFFSET = 10L;
    private static final int PARTITION = 3;
    private static final Long TIMESTAMP = 123456L;
    private static final String TOPIC = "test-topic";
    private static final String VALUE = "test-value";
    private static final int MAX_BLOCK_MILLISECONDS = 1000;
    private static final int REQUEST_TIMEOUT_MILLISECONDS = 1000;

    private CHKafkaProducer producer;
    private Message message;

    @Mock
    private KafkaProducer<String, byte[]> mockKafkaProducer;

    @Mock
    private Future<RecordMetadata> recordMetadataFuture;

    @Mock
    private KafkaProducerFactory mockProducerFactory;

    @BeforeEach
    public void setUp() {
        createTestMessage();

        when(mockProducerFactory.getProducer(any())).thenReturn(mockKafkaProducer);
    }

    @Test
    void testSendAndReturnFuture() {

        createTestProducer(true, Acks.NO_RESPONSE);
        producer.sendAndReturnFuture(message);

        verify(mockKafkaProducer).send(any());
    }

    @Test
    void testSendRoundRobinAcksNoResponse() throws Exception {
        when(mockKafkaProducer.send(any())).thenReturn(recordMetadataFuture);
        createTestProducer(true, Acks.NO_RESPONSE);
        producer.send(message);
        verify(mockKafkaProducer).send(any());
        verify(recordMetadataFuture, times(1)).get();
    }

    @Test
    void testSendManualPartitionAcksNoResponse() throws Exception {
        when(mockKafkaProducer.send(any())).thenReturn(recordMetadataFuture);
        createTestProducer(false, Acks.NO_RESPONSE);
        producer.send(message);
        verify(mockKafkaProducer).send(any());
        verify(recordMetadataFuture, times(1)).get();
    }

    @Test
    void testSendRoundRobinAcksWaitForLocal() throws Exception {
        when(mockKafkaProducer.send(any())).thenReturn(recordMetadataFuture);
        createTestProducer(true, Acks.WAIT_FOR_LOCAL);
        producer.send(message);
        verify(mockKafkaProducer).send(any());
        verify(recordMetadataFuture, times(1)).get();
    }

    @Test
    void testSendRoundRobinAcksWaitForAll() throws Exception {
        when(mockKafkaProducer.send(any())).thenReturn(recordMetadataFuture);
        createTestProducer(true, Acks.WAIT_FOR_ALL);
        producer.send(message);
        verify(mockKafkaProducer).send(any());
        verify(recordMetadataFuture, times(1)).get();
    }

    @Test
    void testCloseRoundRobinAcksNoResponse() {
        createTestProducer(true, Acks.NO_RESPONSE);
        producer.close();
        verify(mockKafkaProducer).close();
    }

    /**
     * Create the test configuration and a producer to test
     *
     * @param roundRobinPartitioner whether to use the round robin partitioner or not
     * @param acks the acks configuration to use
     */
    private void createTestProducer(boolean roundRobinPartitioner, Acks acks) {
        mockApacheKafkaIntegration(roundRobinPartitioner, acks.getCode());

        ProducerConfig config = new ProducerConfig();
        config.setAcks(acks);
        config.setBrokerAddresses(new String[]{TEST_BROKER});
        config.setRetries(TEST_RETRIES);
        config.setRoundRobinPartitioner(roundRobinPartitioner);

        producer = new CHKafkaProducer(config, mockProducerFactory);
    }

    /**
     * Mock interactions with Apache Kafka.
     * <p>
     * Powermock will expect the values in the Properties generated in the test class from the
     * ProducerConfig to match the values in the Properties used as the argument to mock the
     * Kafka producer.
     * </p>
     * @param roundRobinPartitioner whether to use the round robin partitioner or not
     * @param acksCode  the acks configuration string to use
     */
    private void mockApacheKafkaIntegration(boolean roundRobinPartitioner, String acksCode) {
        Properties props = new Properties();
        props.put("bootstrap.servers", String.join(",", new String[]{TEST_BROKER}));
        props.put("acks", acksCode);
        props.put("retries", TEST_RETRIES);
        props.put("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.block.ms", MAX_BLOCK_MILLISECONDS);
        props.put("request.timeout.ms", REQUEST_TIMEOUT_MILLISECONDS);

        if(roundRobinPartitioner) {
            props.put("partition.assignment.strategy", "roundrobin");
        }
    }

    private void createTestMessage() {
        message = new Message();
        message.setKey(KEY);
        message.setOffset(OFFSET);
        message.setPartition(PARTITION);
        message.setTimestamp(TIMESTAMP);
        message.setTopic(TOPIC);
        message.setValue(VALUE.getBytes());
    }

}