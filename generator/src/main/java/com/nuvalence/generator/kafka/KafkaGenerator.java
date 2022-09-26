package com.nuvalence.generator.kafka;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.nuvalence.aggregation.model.Event;
import com.nuvalence.generator.kafka.config.TopologyConfig;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@Configuration
@EnableScheduling
@Slf4j
public class KafkaGenerator {

    private final KafkaTemplate<UUID, Event> kafkaTemplate;

    private final TopologyConfig config;

    private final Random random;

    public KafkaGenerator(KafkaProperties kafkaProperties,
                          TopologyConfig topologyConfig,
                          SchemaRegistryClient registryClient) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        Serializer<Event> serializer = new KafkaProtobufSerializer<>(registryClient);
        properties.put("schema.registry.url", topologyConfig.getSchemaRegistryBaseUrl());
        kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(properties,
                new UUIDSerializer(),
                serializer));
        kafkaTemplate.setDefaultTopic(topologyConfig.getTopic());

        this.config = topologyConfig;
        random = new Random(Instant.now().getNano());
    }

    @Scheduled(initialDelay = 1000, fixedRate = 5000)
    public void publishRecord() {
        UUID myUUID = UUID.randomUUID();
        sendInitEvent(myUUID);
        int numOfContinues = random.nextInt(6);
        for (int i = 0; i <= numOfContinues; i++) {
            sendContinueEvent(myUUID);
        }
        sendTerminalEvent(myUUID);
    }

    private void sendInitEvent(UUID myUUID) {
        Event initEvent = Event.newBuilder()
                .setId(bytesFromUUID(myUUID))
                .setType(Event.TYPE.INITIALIZING)
                .setTtl(Duration.newBuilder().setSeconds(60).build())
                .setMessage(String.format("Init message created at %s, with nano %d",
                        Instant.now().toString(), Instant.now().getNano()))
                .build();
        kafkaTemplate.send(new ProducerRecord<>(config.getTopic(), myUUID, initEvent));
    }

    private void sendContinueEvent(UUID myUUID) {
        Event continueEvent = Event.newBuilder()
                .setId(bytesFromUUID(myUUID))
                .setType(Event.TYPE.CONTINUING)
                .setMessage(String.format("Continue message created at %s, with nano %d",
                        Instant.now().toString(), Instant.now().getNano()))
                .build();
        kafkaTemplate.send(new ProducerRecord<>(config.getTopic(), myUUID, continueEvent));
    }

    private void sendTerminalEvent(UUID myUUID) {
        Event terminalEvent = Event.newBuilder()
                .setId(bytesFromUUID(myUUID))
                .setType(Event.TYPE.TERMINATING)
                .setMessage(String.format("Terminal message created at %s, with nano %d",
                        Instant.now().toString(), Instant.now().getNano()))
                .build();
        kafkaTemplate.send(new ProducerRecord<>(config.getTopic(), myUUID, terminalEvent));
    }

    private static ByteString bytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return ByteString.copyFrom(bb.array());
    }
}
