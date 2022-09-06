package com.nuvalence.aggregation.kstream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Configuration
public class TestConfig {

    @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
    private String brokerAddresses;

    @Bean
    public KafkaTemplate<Integer, String> template() {
        KafkaTemplate<Integer, String> kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerConfigs()));
        kafkaTemplate.setDefaultTopic("intopic");
        return kafkaTemplate;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        return KafkaTestUtils.producerProps(brokerAddresses);
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        return KafkaTestUtils.consumerProps(this.brokerAddresses, "testGroup", "false");
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public CompletableFuture<ConsumerRecord<?, String>> resultFuture() {
        return new CompletableFuture<>();
    }

    @KafkaListener(topics = "${topology.outputTopicName}")
    public void listener(ConsumerRecord<?, String> payload) {
        resultFuture().complete(payload);
    }
}
