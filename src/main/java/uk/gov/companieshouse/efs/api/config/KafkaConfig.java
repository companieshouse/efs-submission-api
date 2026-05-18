package uk.gov.companieshouse.efs.api.config;

import org.apache.avro.Schema;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.efs.api.kafka.CHKafkaProducer;
import uk.gov.companieshouse.efs.api.client.EfsRestClient;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

/**
 * Configuration class for the kafka queue used to send emails.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.config.acks}")
    private String acks;

    @Value("${kafka.config.retries:10}")
    private int retries;

    @Value("${kafka.config.isRoundRobin}")
    private boolean isRoundRobin;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${kafka.schema.uri.email-send}")
    private String emailSchemaUri;

    /**
     * Default no-argument constructor required by Spring for instantiating the configuration class.
     */
    public KafkaConfig() {
        // Intentionally blank
    }

    @Bean
    CHKafkaProducer producer(final ProducerConfig producerConfig) {
        return new CHKafkaProducer(producerConfig);
    }

    @Bean
    ProducerConfig producerConfig() {
        final var config = new ProducerConfig();

        ProducerConfigHelper.assignBrokerAddresses(config);
        config.setAcks(Acks.valueOf(acks));
        config.setRoundRobinPartitioner(isRoundRobin);
        config.setRetries(retries);

        return config;
    }

    /**
     * Provides an EfsRestClient bean that wraps Spring's RestClient for HTTP operations.
     * All errors are surfaced as exceptions and must be handled by the caller.
     */
    @Bean
    public EfsRestClient efsRestClient(final RestClient restClient) {
        return new EfsRestClient(restClient);
    }

    @Bean
    public Schema fetchSchema(final EfsRestClient efsRestClient) {
        final var bytes = efsRestClient.getSchema(schemaRegistryUrl, emailSchemaUri);
        final var schemaJson = new JSONObject(new String(bytes)).getString("schema");

        return new Schema.Parser().parse(schemaJson);
    }

}