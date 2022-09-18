package com.nuvalence.generator.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KafkaGenerator {

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    @Value("${kafka-aggregation.topic:aggregated-states}")
    private String topic;

    public KafkaGenerator(KafkaProperties kafkaProperties) {
        kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties()));
        kafkaTemplate.setDefaultTopic(topic);
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5000)
    public void publishRecord() {
        kafkaTemplate.send(new ProducerRecord<>(topic, 1, "Hello, World!"));
    }
}