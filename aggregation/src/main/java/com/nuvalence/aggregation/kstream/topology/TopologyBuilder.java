package com.nuvalence.aggregation.kstream.topology;

import com.nuvalence.aggregation.kstream.config.TopologyConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TopologyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TopologyBuilder.class);

    private final StreamsBuilder streamsBuilder = new StreamsBuilder();
    private final TopologyConfig config;

    public TopologyBuilder(TopologyConfig config) {
        this.config = config;
    }

    public Topology constructAggregationTopology() {
        streamsBuilder.stream(config.getInputTopicName())
                .filter((key, value) -> {
                    logger.info("I Saw the record!!!!!!");
                    return true;
                })
                .to(config.getOutputTopicName());

        return streamsBuilder.build();
    }
}
