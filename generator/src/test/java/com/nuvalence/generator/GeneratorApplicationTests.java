package com.nuvalence.generator;

import com.nuvalence.generator.kafka.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(classes = {GeneratorApplication.class, TestConfig.class})
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
