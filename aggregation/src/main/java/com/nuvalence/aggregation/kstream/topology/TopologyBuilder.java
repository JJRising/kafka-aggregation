package com.nuvalence.aggregation.kstream.topology;

import com.nuvalence.aggregation.kstream.config.TopologyConfig;
import com.nuvalence.aggregation.kstream.processor.AggregationProcessor;
import com.nuvalence.aggregation.kstream.processor.LostTransformer;
import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.StreamsConfig;
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
    public static final String LOST_TRANSFORMER = "Lost Transformer";
    public static final String LOST_SINK = "Lost Sink";

    private final TopologyConfig config;
    private final StreamsConfig streamsConfig;
    private final SchemaRegistryClient schemaRegistryClient;

    public TopologyBuilder(TopologyConfig config,
                           StreamsConfig streamsConfig,
                           SchemaRegistryClient schemaRegistryClient) {
        this.config = config;
        this.streamsConfig = streamsConfig;
        this.schemaRegistryClient = schemaRegistryClient;
    }

    public Topology constructAggregationTopology() {
        return new Topology()
                .addSource(EVENT_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Event.class),
                        config.getInputTopicName())
                .addProcessor(AGGREGATION_PROCESSOR, AggregationProcessor::new, EVENT_SOURCE)
                .addStateStore(aggregationStoreBuilder())
                .addSink(AGGREGATED_SINK,
                        config.getAggregatedTopicName(),
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<Aggregation>(schemaRegistryClient, streamsConfig.originals()),
                        AGGREGATION_PROCESSOR)
                .addProcessor(LOST_TRANSFORMER, LostTransformer::new, AGGREGATION_PROCESSOR)
                .addSink(LOST_SINK,
                        config.getLostTopicName(),
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<Event>(schemaRegistryClient, streamsConfig.originals()),
                        LOST_TRANSFORMER);
    }

    private StoreBuilder<KeyValueStore<UUID, Aggregation>> aggregationStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(AGGREGATION_STORE),
                Serdes.UUID(),
                new KafkaProtobufSerde<>());
    }
}
