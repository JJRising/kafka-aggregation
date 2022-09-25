package com.nuvalence.aggregation.kstream.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka-topology")
public class TopologyConfig {

    private String inputTopicName;
    private String aggregatedTopicName;
    private String lostTopicName;

    private Map<String, String> properties = new HashMap<>();
    private String schemaRegistryBaseUrl;
    private int schemaRegistryClientCacheCapacity = 1000;

    private Duration shutdownTimeout = Duration.ZERO;
    private StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse streamThreadExceptionResponse =
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;

    @Bean(name = "schemaRegistryClient")
    public SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(schemaRegistryBaseUrl, schemaRegistryClientCacheCapacity);
    }

    @Bean
    public StreamsConfig streamsConfig() {
        return new StreamsConfig(Stream.of(properties).collect(Properties::new, Map::putAll, Map::putAll));
    }
}
