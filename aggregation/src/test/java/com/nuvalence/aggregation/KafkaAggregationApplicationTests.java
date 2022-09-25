package com.nuvalence.aggregation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
		partitions = 1,
		topics = {
				"${kafka-topology.inputTopicName}",
				"${kafka-topology.aggregatedTopicName}",
				"${kafka-topology.lostTopicName}"
		},
		brokerPropertiesLocation = "classpath:/broker.properties")
class KafkaAggregationApplicationTests {

	@Test
	void contextLoads() {
	}

}
