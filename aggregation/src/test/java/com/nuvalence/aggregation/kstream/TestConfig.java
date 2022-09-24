package com.nuvalence.aggregation.kstream;

import com.nuvalence.aggregation.models.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@TestConfiguration
public class TestConfig {

    @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
    private String brokerAddresses;

    @Bean(name = "schemaRegistryClient")
    public SchemaRegistryClient schemaRegistryClient() {
        return MockSchemaRegistry.getClientForScope("fake");
    }

    @Bean
    public KafkaTemplate<UUID, Event> template() {
        KafkaTemplate<UUID, Event> kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerConfigs()));
        kafkaTemplate.setDefaultTopic("events");
        return kafkaTemplate;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> ret =  KafkaTestUtils.producerProps(brokerAddresses);
        ret.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        ret.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
        ret.put("schema.registry.url", "mock://fake");
        return ret;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> ret = KafkaTestUtils.consumerProps(this.brokerAddresses, "testGroup", "false");
        ret.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        ret.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class);
        ret.put("schema.registry.url", "mock://fake");
        return ret;
    }

    @Bean
    public ConsumerFactory<UUID, Event> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<UUID, Event>>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<UUID, Event> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public CompletableFuture<ConsumerRecord<UUID, Event>> resultFuture() {
        return new CompletableFuture<>();
    }

    @KafkaListener(topics = "${topology.lostTopicName}")
    public void listener(ConsumerRecord<UUID, Event> payload) {
        resultFuture().complete(payload);
    }
}
