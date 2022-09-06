package com.nuvalence.aggregation.kstream;

import com.nuvalence.aggregation.KafkaAggregationApplication;
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

@SpringBootTest(classes = KafkaAggregationApplication.class)
@EmbeddedKafka(
        partitions = 1,
		topics = {
				"${topology.inputTopicName}",
				"${topology.outputTopicName}"
		},
        brokerPropertiesLocation = "classpath:/broker.properties")
public class TopologyTest {
    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private CompletableFuture<ConsumerRecord<?, String>> resultFuture;

    @Test
    public void testKStreams() throws ExecutionException, InterruptedException, TimeoutException {
        String myUUID = UUID.randomUUID().toString();
        this.kafkaTemplate.sendDefault(0, myUUID);
        ConsumerRecord<?, String> result = resultFuture.get(600, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals("outtopic", result.topic());
        assertEquals(myUUID, result.value());
    }
}
