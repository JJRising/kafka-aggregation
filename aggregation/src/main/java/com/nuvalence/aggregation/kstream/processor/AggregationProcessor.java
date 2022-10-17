package com.nuvalence.aggregation.kstream.processor;

import com.google.protobuf.Timestamp;
import com.nuvalence.aggregation.kstream.processor.config.AggregationProcessorConfig;
import com.nuvalence.aggregation.kstream.topology.TopologyBuilder;
import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.nuvalence.aggregation.kstream.topology.TopologyBuilder.AGGREGATED_SINK;
import static com.nuvalence.aggregation.kstream.topology.TopologyBuilder.LOST_TRANSFORMER;
import static com.nuvalence.aggregation.model.Event.TYPE.*;
import static com.nuvalence.aggregation.utils.UUIDUtils.bytesFromUUID;
import static com.nuvalence.aggregation.utils.UUIDUtils.uuidFromBytes;

public class AggregationProcessor implements Processor<UUID, Event, UUID, Aggregation> {

    private final AggregationProcessorConfig config;

    private ProcessorContext<UUID, Aggregation> context;
    private KeyValueStore<UUID, Aggregation> aggregationStore;

    public AggregationProcessor(AggregationProcessorConfig aggregationProcessorConfig) {
        this.config = aggregationProcessorConfig;
    }

    @Override
    public void init(ProcessorContext<UUID, Aggregation> context) {
        this.context = context;
        this.aggregationStore = context.getStateStore(TopologyBuilder.AGGREGATION_STORE);

        // Establish the expiration processor to review aggregations in the store, and expiring stale ones,
        // pushing them to be persisted.
        context.schedule(Duration.ofSeconds(config.getExpiryProcessIntervalSeconds()),
                PunctuationType.STREAM_TIME,
                timestamp -> {
                    Instant now = Instant.now();
                    aggregationStore.all().forEachRemaining((entry) -> {
                        if (shouldExpire(entry, now)) {
                            expireAndForward(entry.value);
                            aggregationStore.delete(entry.key);
                        }
                    });
                });
    }

    private static boolean shouldExpire(KeyValue<UUID, Aggregation> entry, Instant now) {
        return Instant.ofEpochSecond(
                        entry.value.getEndTime().getSeconds(),
                        entry.value.getEndTime().getNanos())
                .isBefore(now);
    }

    private void expireAndForward(Aggregation aggregation) {
        Record<UUID, Aggregation> output = new Record<>(
                uuidFromBytes(aggregation.getId()),
                aggregation.toBuilder()
                        .addEvents(
                                Event.newBuilder().setId(aggregation.getId())
                                        .setTypeValue(TERMINATING.getNumber())
                                        .setMessage("The event chain has expired and will be persisted.")
                        ).build(),
                Instant.now().toEpochMilli()
        );
        context.forward(output, AGGREGATED_SINK);
    }

    // Init events create a new store entry if one does not yet exist
    // Init events are added to the store entry if one does exist
    // Continue events are added to the store entry
    // Terminal events push the entry out
    // Continue/Terminal events that do not have a store entry are pushed as Lost
    @Override
    public void process(Record<UUID, Event> record) {
        Aggregation existingEntry = aggregationStore.get(record.key());
        switch (record.value().getTypeValue()) {
            case INITIALIZING_VALUE -> {
                if (existingEntry != null) {
                    aggregationStore.put(record.key(), aggregate(existingEntry, record.value()));
                } else {
                    aggregationStore.put(record.key(), newAggregation(record.value()));
                }
            }
            case CONTINUING_VALUE -> {
                if (existingEntry != null) {
                    aggregationStore.put(record.key(), aggregate(existingEntry, record.value()));
                } else {
                    forwardAsLost(record);
                }
            }
            case TERMINATING_VALUE -> {
                if (existingEntry != null) {
                    Aggregation aggregation = aggregate(existingEntry, record.value());
                    forwardAggregation(aggregation);
                    aggregationStore.delete(record.key());
                } else {
                    forwardAsLost(record);
                }
            }
        }
    }

    private Aggregation newAggregation(Event event) {
        Instant now = Instant.now();
        Timestamp endTime = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond() + event.getTtl().getSeconds())
                .setNanos(now.getNano() + event.getTtl().getNanos())
                .build();

        return Aggregation.newBuilder()
                .setId(event.getId())
                .setEndTime(endTime)
                .addEvents(event)
                .build();
    }

    private Aggregation aggregate(Aggregation aggregation, Event event) {
        return aggregation.toBuilder()
                .addEvents(event)
                .build();
    }

    private void forwardAggregation(Aggregation aggregation) {
        Record<UUID, Aggregation> output = new Record<>(
                uuidFromBytes(aggregation.getId()),
                aggregation,
                Instant.now().toEpochMilli()
        );
        context.forward(output, AGGREGATED_SINK);
    }

    private void forwardAsLost(Record<UUID, Event> record) {
        Record<UUID, Aggregation> output = new Record<>(
                record.key(),
                Aggregation.newBuilder()
                        .setId(bytesFromUUID(record.key()))
                        .addEvents(record.value())
                        .build(),
                Instant.now().toEpochMilli()
        );
        context.forward(output, LOST_TRANSFORMER);
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
