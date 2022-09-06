package com.nuvalence.aggregation.kstream;

import com.nuvalence.aggregation.kstream.config.StreamsConfig;
import com.nuvalence.aggregation.kstream.topology.TopologyBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class StreamsManager {

    private static final Logger logger = LoggerFactory.getLogger(StreamsManager.class);

    private final TopologyBuilder topologyBuilder;
    private final StreamsConfig streamsProperties;

    private KafkaStreams streams;

    public StreamsManager(TopologyBuilder topologyBuilder, StreamsConfig streamsProperties) {
        this.topologyBuilder = topologyBuilder;
        this.streamsProperties = streamsProperties;
    }

    @PostConstruct
    private void init() {
        Topology topology = topologyBuilder.constructAggregationTopology();
        streams = new KafkaStreams(topology, streamsProperties.getKafkaProperties());
        streams.setUncaughtExceptionHandler(uncaughtExceptionHandler());
        streams.start();
        logger.info("Started Kafka Streams.");
    }

    @PreDestroy
    public void onExit() {
        logger.error("Received shutdown hook. Attempting to gracefully close KStreams.");
        streams.close();
    }

    private StreamsUncaughtExceptionHandler uncaughtExceptionHandler() {
        return (Throwable throwable) -> {
            logger.error(
                    String.format("An exception was thrown within the Streams thread. " +
                            "Performing configured behaviour: %s",
                            streamsProperties.getStreamThreadExceptionResponse().name),
                    throwable);
            return streamsProperties.getStreamThreadExceptionResponse();
        };
    }
}
