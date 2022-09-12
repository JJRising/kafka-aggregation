package com.nuvalence.aggregation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class KafkaAggregationApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAggregationApplication.class, args);
	}

}
