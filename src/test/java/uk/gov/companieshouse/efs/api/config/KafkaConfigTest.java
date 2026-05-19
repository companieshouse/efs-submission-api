package uk.gov.companieshouse.efs.api.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.apache.avro.Schema;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.efs.api.client.EfsRestClient;
import uk.gov.companieshouse.efs.api.kafka.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @Mock
    private RestClient restClient;

    @Mock
    private EfsRestClient efsRestClient;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "acks", "WAIT_FOR_ALL");
        ReflectionTestUtils.setField(kafkaConfig, "retries", 7);
        ReflectionTestUtils.setField(kafkaConfig, "isRoundRobin", true);
        ReflectionTestUtils.setField(kafkaConfig, "schemaRegistryUrl", "http://schema-registry");
        ReflectionTestUtils.setField(kafkaConfig, "emailSchemaUri", "/schemas/ids/1");
    }

    @Test
    void producerCreatesProducerBean() {
        final var producerConfig = new ProducerConfig();
        producerConfig.setBrokerAddresses(new String[] {"localhost:9092"});
        producerConfig.setAcks(Acks.WAIT_FOR_ALL);

        final var producer = kafkaConfig.producer(producerConfig);

        assertThat(producer, isA(CHKafkaProducer.class));
    }

    @Test
    void producerConfigAppliesConfiguredValues() {
        try (final var mockedStatic = mockStatic(ProducerConfigHelper.class)) {
            final var config = kafkaConfig.producerConfig();

            mockedStatic.verify(() -> ProducerConfigHelper.assignBrokerAddresses(any(ProducerConfig.class)));
            assertThat(config.getAcks(), is(Acks.WAIT_FOR_ALL));
            assertThat(config.getRetries(), is(7));
            assertThat(config.isRoundRobinPartitioner(), is(true));
        }
    }

    @Test
    void efsRestClientWrapsRestClient() {
        final var client = kafkaConfig.efsRestClient(restClient);

        assertThat(client, isA(EfsRestClient.class));
    }

    @Test
    void fetchSchemaParsesAvroSchemaFromJsonPayload() {
        final var schemaJson = """
            {
              "type": "record",
              "name": "EmailSend",
              "fields": []
            }
            """;
        final var schemaPayload = new JSONObject()
            .put("schema", schemaJson)
            .toString();

        when(efsRestClient.getSchema("http://schema-registry", "/schemas/ids/1")).thenReturn(schemaPayload.getBytes());

        final var schema = kafkaConfig.fetchSchema(efsRestClient);

        assertThat(schema, isA(Schema.class));
        assertThat(schema.getName(), is("EmailSend"));
    }
}

