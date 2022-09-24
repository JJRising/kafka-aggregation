package com.nuvalence.aggregation.kstream.processor;

import com.nuvalence.aggregation.kstream.topology.TopologyBuilder;
import com.nuvalence.aggregation.models.Aggregation;
import com.nuvalence.aggregation.models.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.Collections;
import java.util.UUID;

public class AggregationProcessor implements Processor<UUID, Event, UUID, Aggregation> {

    private ProcessorContext<UUID, Aggregation> context;

    @Override
    public void init(ProcessorContext<UUID, Aggregation> context) {
        Processor.super.init(context);
        // TODO implement the Lost processor, going over the aggregation store and pushing out any items
        //  that have surpassed their ttl.
        this.context = context;
    }

    @Override
    public void process(Record<UUID, Event> record) {
        // TODO implement the processor based on the following rules
        // Init events create a new store entry
        // Continue events are added to the store entry
        // terminal events push the entry out
        // Continue events that do not have a store entry are pushed as Lost
        Record<UUID, Aggregation> output = new Record<>(
                record.key(),
                new Aggregation(record.key(), Collections.singletonList(record.value())),
                record.timestamp(),
                record.headers()
        );
        context.forward(output, TopologyBuilder.LOST_SINK);
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
