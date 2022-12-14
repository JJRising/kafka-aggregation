package com.nuvalence.aggregation.kstream.processor;

import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

/**
 * This simple class converts the Aggregation that was wrapped and outputted by the
 * AggregationProcessor back to a standalone event.
 */
public class LostTransformer implements Processor<UUID, Aggregation, UUID, Event> {

    private ProcessorContext<UUID, Event> context;

    @Override
    public void init(ProcessorContext<UUID, Event> context) {
        this.context = context;
    }

    @Override
    public void process(Record<UUID, Aggregation> record) {
        context.forward(
                new Record<>(
                        record.key(),
                        record.value().getEvents(0),
                        record.timestamp(),
                        record.headers()));
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
