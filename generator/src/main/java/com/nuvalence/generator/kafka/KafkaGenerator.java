package com.nuvalence.generator.kafka;

import com.nuvalence.generator.model.Book;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Component
@EnableScheduling
@Slf4j
public class KafkaGenerator {

    private final KafkaTemplate<Integer, Object> kafkaTemplate;

    @Value("${kafka-aggregation.topic:aggregated-states}")
    private String topic;

    public KafkaGenerator(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
        properties.put("schema.registry.url", "http://schema-registry-headless.default.svc.cluster.local:8081");
        kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(properties));
        kafkaTemplate.setDefaultTopic(topic);
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5000)
    public void publishRecord() {
        log.info("Publishing Hello World message");
        kafkaTemplate.send(new ProducerRecord<>(topic, 1, new Book("Hello, World!")));
    }
}
