package com.nuvalence.aggregation.kstream.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "topology")
public class TopologyConfig {

    private String inputTopicName;
    private String outputTopicName;
}
