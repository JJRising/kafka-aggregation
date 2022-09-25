package com.nuvalence.aggregation.kstream.topology;

import com.nuvalence.aggregation.kstream.config.TopologyConfig;
import com.nuvalence.aggregation.kstream.processor.AggregationProcessor;
import com.nuvalence.aggregation.kstream.processor.LostTransformer;
import com.nuvalence.aggregation.kstream.processor.config.AggregationProcessorConfig;
import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Component
public class TopologyBuilder {

    public static final String EVENT_SOURCE = "event-source";
    public static final String AGGREGATION_PROCESSOR = "aggregation-processor";
    public static final String AGGREGATION_STORE = "aggregation-store";
    public static final String AGGREGATED_SINK = "aggregated-sink";
    public static final String LOST_TRANSFORMER = "lost-transformer";
    public static final String LOST_SINK = "lost-sink";

    private final ApplicationContext applicationContext;
    private final TopologyConfig config;
    private final StreamsConfig streamsConfig;
    private final SchemaRegistryClient schemaRegistryClient;

    public TopologyBuilder(ApplicationContext applicationContext,
                           TopologyConfig config,
                           StreamsConfig streamsConfig,
                           SchemaRegistryClient schemaRegistryClient) {
        this.applicationContext = applicationContext;
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
                .addProcessor(AGGREGATION_PROCESSOR, new ProcessorSupplier<UUID, Event, UUID, Aggregation>() {
                    @Override
                    public Processor<UUID, Event, UUID, Aggregation> get() {
                        return new AggregationProcessor(applicationContext.getBean(AggregationProcessorConfig.class));
                    }
                    @Override
                    public Set<StoreBuilder<?>> stores() {
                        return Collections.singleton(aggregationStoreBuilder());
                    }
                }, EVENT_SOURCE)
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
        Serde<Aggregation> valueSerde = new KafkaProtobufSerde<>(schemaRegistryClient, Aggregation.class);
        valueSerde.configure(streamsConfig.originals(), false);
        return Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(AGGREGATION_STORE),
                Serdes.UUID(),
                valueSerde);
    }
}
