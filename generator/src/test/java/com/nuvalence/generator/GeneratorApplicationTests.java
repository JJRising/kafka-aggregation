package com.nuvalence.generator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
		partitions = 1,
		topics = {
				"aggregated-states"
		},
		brokerPropertiesLocation = "classpath:/broker.properties")
class GeneratorApplicationTests {

	@Test
	void contextLoads() {
	}

}
