package com.nuvalence.aggregation.kstream.processor;

import com.nuvalence.aggregation.models.Aggregation;
import com.nuvalence.aggregation.models.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

public class LostTransformer implements Processor<UUID, Aggregation, UUID, Event> {

    private ProcessorContext<UUID, Event> context;

    @Override
    public void init(ProcessorContext<UUID, Event> context) {
        Processor.super.init(context);
        this.context = context;
    }

    @Override
    public void process(Record<UUID, Aggregation> record) {
        context.forward(
                new Record<>(
                        record.key(),
                        record.value().getEvents().get(0),
                        record.timestamp(),
                        record.headers()));
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
