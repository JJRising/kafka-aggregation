package com.nuvalence.aggregation.kstream.processor;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.nuvalence.aggregation.kstream.processor.config.AggregationProcessorConfig;
import com.nuvalence.aggregation.kstream.topology.TopologyBuilder;
import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static com.nuvalence.aggregation.utils.UUIDUtils.bytesFromUUID;
import static org.junit.jupiter.api.Assertions.*;

class AggregationProcessorTest {

    private static AggregationProcessor subject;
    private static MockProcessorContext<UUID, Aggregation> context;

    @BeforeAll
    public static void init() {
        SchemaRegistryClient mockSchemaRegistryClient = MockSchemaRegistry.getClientForScope("fake");
        KafkaProtobufSerde<Aggregation> serde = new KafkaProtobufSerde<>(mockSchemaRegistryClient, Aggregation.class);
        serde.configure(new StreamsConfig(Map.of(
                "application.id", "testApp",
                "bootstrap.servers", "fake",
                "schema.registry.url", "mock://fake"
        )).originals(), false);

        AggregationProcessorConfig config = new AggregationProcessorConfig();
        config.setExpiryProcessIntervalSeconds(10);
        subject = new AggregationProcessor(config);
        context = new MockProcessorContext<>();

        StateStore store = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(TopologyBuilder.AGGREGATION_STORE),
                Serdes.UUID(),
                serde)
                .withLoggingDisabled()
                .build();
        store.init(context.getStateStoreContext(), store);
        context.addStateStore(store);

        subject.init(context);
    }

    // Three events are aggregated
    @Test
    void process() {
        UUID myUUID = UUID.randomUUID();
        ByteString myUUIDbytes = bytesFromUUID(myUUID);
        Event initEvent = Event.newBuilder()
                .setId(myUUIDbytes)
                .setType(Event.TYPE.INITIALIZING)
                .setTtl(Duration.newBuilder().setSeconds(300).build())
                .setMessage("Started a new Aggregation")
                .build();
        Record<UUID, Event> initRecord = new Record<>(myUUID, initEvent, Instant.now().toEpochMilli());

        Event continueEvent = Event.newBuilder()
                .setId(myUUIDbytes)
                .setType(Event.TYPE.CONTINUING)
                .setTtl(Duration.newBuilder().setSeconds(300).build())
                .setMessage("Continue the aggregation")
                .build();
        Record<UUID, Event> continueRecord = new Record<>(myUUID, continueEvent, Instant.now().toEpochMilli());

        Event terminalEvent = Event.newBuilder()
                .setId(myUUIDbytes)
                .setType(Event.TYPE.TERMINATING)
                .setTtl(Duration.newBuilder().setSeconds(300).build())
                .setMessage("Finish the aggregation")
                .build();
        Record<UUID, Event> terminalRecord = new Record<>(myUUID, terminalEvent, Instant.now().toEpochMilli());

        subject.process(initRecord);
        subject.process(continueRecord);
        subject.process(terminalRecord);

        Iterator<MockProcessorContext.CapturedForward<? extends UUID, ? extends Aggregation>> forwarded
                = context.forwarded().iterator();

        Record<? extends UUID, ? extends Aggregation> output = forwarded.next().record();
        assertEquals(myUUID, output.key());
        assertEquals(3, output.value().getEventsList().size());

        assertFalse(forwarded.hasNext());
    }
}