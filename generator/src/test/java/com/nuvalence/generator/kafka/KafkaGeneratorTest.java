package com.nuvalence.generator.kafka;

import com.nuvalence.generator.GeneratorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "aggregated-states"
        },
        brokerPropertiesLocation = "classpath:/broker.properties")
public class KafkaGeneratorTest {

    @Autowired
    KafkaGenerator kafkaGenerator;

    @Test
    public void publishRecordTest() {
        assertDoesNotThrow(() -> kafkaGenerator.publishRecord());
    }

    @TestConfiguration
    public static class Config {
        @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private String brokerAddresses;

        @Bean(name = "producerConfigs")
        public Map<String, Object> producerConfigs() {
            return KafkaTestUtils.producerProps(brokerAddresses);
        }
    }
}
