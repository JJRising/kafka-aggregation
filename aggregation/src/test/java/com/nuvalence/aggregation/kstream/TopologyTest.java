package com.nuvalence.aggregation.kstream;

import com.nuvalence.aggregation.KafkaAggregationApplication;
import com.nuvalence.aggregation.models.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {KafkaAggregationApplication.class, TestConfig.class})
@EmbeddedKafka(
        partitions = 1,
		topics = {
                "${topology.inputTopicName}",
                "${topology.aggregatedTopicName}",
                "${topology.lostTopicName}"
		},
        brokerPropertiesLocation = "classpath:/broker.properties")
public class TopologyTest {
    @Autowired
    private KafkaTemplate<UUID, Event> kafkaTemplate;

    @Autowired
    private CompletableFuture<ConsumerRecord<UUID, Event>> resultFuture;

    @Test
    public void testKStreams() throws ExecutionException, InterruptedException, TimeoutException {
        UUID myUUID = UUID.randomUUID();
        Event myEvent = new Event(myUUID, Event.Type.CONTINUING, "Hello, World!");
        this.kafkaTemplate.sendDefault(myUUID, myEvent);
        ConsumerRecord<UUID, Event> result = resultFuture.get(600, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(myUUID, result.key());
        assertEquals(myEvent, result.value());
    }
}
