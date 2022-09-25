package com.nuvalence.aggregation.kstream.processor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka-topology.aggregation-processor")
@Getter
@Setter
public class AggregationProcessorConfig {

    private int expiryProcessIntervalSeconds = 1;
}
