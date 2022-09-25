package com.nuvalence.aggregation.kstream.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class StreamsProperties {

    // Kafka-native configs
    private Map<String, String> properties = new HashMap<>();

    // Non Kafka-native configs
    private Duration shutdownTimeout = Duration.ZERO;
    private StreamThreadExceptionResponse streamThreadExceptionResponse = StreamThreadExceptionResponse.REPLACE_THREAD;

    public Properties getKafkaProperties() {
        Properties ret = Stream.of(properties).collect(Properties::new, Map::putAll, Map::putAll);
        ret.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        ret.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        ret.put(SCHEMA_REGISTRY_URL_CONFIG, "mock://fake");
        return ret;
    }

    @Bean
    public StreamsConfig streamsConfig() {
        Properties ret = Stream.of(properties).collect(Properties::new, Map::putAll, Map::putAll);
        ret.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        ret.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        ret.put(SCHEMA_REGISTRY_URL_CONFIG, "mock://fake");
        return new StreamsConfig(ret);
    }
}
