package com.nuvalence.aggregation.kstream.topology;

import com.nuvalence.aggregation.kstream.config.TopologyConfig;
import com.nuvalence.aggregation.kstream.processor.AggregationProcessor;
import com.nuvalence.aggregation.kstream.processor.LostTransformer;
import com.nuvalence.aggregation.models.Aggregation;
import com.nuvalence.aggregation.models.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TopologyBuilder {

    public static final String EVENT_SOURCE = "Event Source";
    public static final String AGGREGATION_PROCESSOR = "Aggregation Processor";
    public static final String AGGREGATION_STORE = "Aggregation Store";
    public static final String AGGREGATED_SINK = "Aggregated Sink";
    public static final String LOST_SINK = "Lost Sink";

    private final TopologyConfig config;
    private final SchemaRegistryClient schemaRegistryClient;

    public TopologyBuilder(TopologyConfig config, SchemaRegistryClient schemaRegistryClient) {
        this.config = config;
        this.schemaRegistryClient = schemaRegistryClient;
    }

    public Topology constructAggregationTopology() {
        return new Topology()
                .addSource(EVENT_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaJsonSchemaDeserializer<Event>(schemaRegistryClient),
                        config.getInputTopicName())
                .addProcessor(AGGREGATION_PROCESSOR, AggregationProcessor::new, EVENT_SOURCE)
                .addStateStore(aggregationStoreBuilder())
                .addSink(AGGREGATED_SINK,
                        config.getAggregatedTopicName(),
                        new UUIDSerializer(),
                        new KafkaJsonSchemaSerializer<>(schemaRegistryClient),
                        AGGREGATION_PROCESSOR)
                .addProcessor("Lost Transformer", LostTransformer::new, AGGREGATION_PROCESSOR)
                .addSink(LOST_SINK,
                        config.getLostTopicName(),
                        new UUIDSerializer(),
                        new KafkaJsonSchemaSerializer<>(schemaRegistryClient),
                        AGGREGATION_PROCESSOR);
    }

    private StoreBuilder<KeyValueStore<UUID, Aggregation>> aggregationStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(AGGREGATION_STORE),
                Serdes.UUID(),
                new KafkaJsonSchemaSerde<>());
    }
}
