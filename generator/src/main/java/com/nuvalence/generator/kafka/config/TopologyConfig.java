package com.nuvalence.generator.kafka.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "topology")
public class TopologyConfig {

    private String topic;
    private String schemaRegistryBaseUrl;

    @Bean(name = "schemaRegistryClient")
    public SchemaRegistryClient registryClient() {
        return new CachedSchemaRegistryClient(
                schemaRegistryBaseUrl,
                1000);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSchemaRegistryBaseUrl() {
        return schemaRegistryBaseUrl;
    }

    public void setSchemaRegistryBaseUrl(String schemaRegistryBaseUrl) {
        this.schemaRegistryBaseUrl = schemaRegistryBaseUrl;
    }
}
