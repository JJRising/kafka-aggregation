package com.nuvalence.aggregation.kstream.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "topology")
public class TopologyConfig {

    private String inputTopicName;
    private String aggregatedTopicName;
    private String lostTopicName;
    private String schemaRegistryBaseUrl;
    private int schemaRegistryClientCacheCapacity = 1000;

    @Bean(name = "schemaRegistryClient")
    public SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(schemaRegistryBaseUrl, schemaRegistryClientCacheCapacity);
    }
}
