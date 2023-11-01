package uk.gov.companieshouse.efs.api;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

public class TestKafkaConsumer {

    public static boolean hasEntries(String server, long timeout){
        try(KafkaConsumer<?, ?> consumer = new KafkaConsumer<>(
                ImmutableMap.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server,
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
                ), new StringDeserializer(), new StringDeserializer())) {
            consumer.assign(Collections.singletonList(new TopicPartition("email-send", 0)));
            int duration = 0;
            while (duration < timeout) {
                ConsumerRecords<?, ?> records = consumer.poll(Duration.ofSeconds(2).toMillis());
                if (records.count() > 0) {
                    return true;
                }
                duration++;
            }
        }
        return false;
    }
}
