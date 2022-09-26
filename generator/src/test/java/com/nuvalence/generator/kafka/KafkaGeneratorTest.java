package com.nuvalence.generator.kafka;

import com.nuvalence.generator.GeneratorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = {GeneratorApplication.class, TestConfig.class})
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "events"
        },
        brokerPropertiesLocation = "classpath:/broker.properties")
public class KafkaGeneratorTest {

    @Autowired
    KafkaGenerator kafkaGenerator;

    @Test
    public void publishRecordTest() {
        assertDoesNotThrow(() -> kafkaGenerator.publishRecord());
    }
}
