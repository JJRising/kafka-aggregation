package com.nuvalence.aggregation.kstream.processor;

import com.google.protobuf.Duration;
import com.nuvalence.aggregation.model.Aggregation;
import com.nuvalence.aggregation.model.Event;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

import static com.nuvalence.aggregation.utils.UUIDUtils.bytesFromUUID;
import static org.junit.jupiter.api.Assertions.*;

class LostTransformerTest {

    private static LostTransformer subject;
    private static MockProcessorContext<UUID, Event> context;

    @BeforeAll
    public static void init() {
        subject = new LostTransformer();
        context = new MockProcessorContext<>();
        subject.init(context);
    }

    // Unwrap a lost event from its aggregation
    @Test
    void process() {
        UUID myUUID = UUID.randomUUID();
        Event event = Event.newBuilder()
                .setId(bytesFromUUID(myUUID))
                .setTtl(Duration.newBuilder().setSeconds(21L).setNanos(123).build())
                .setMessage("Hello, World!")
                .build();
        Aggregation aggregation = Aggregation.newBuilder()
                .setId(bytesFromUUID(myUUID))
                .addEvents(event)
                .build();
        Instant now = Instant.now();

        Record<UUID, Aggregation> input = new Record<>(myUUID, aggregation, now.toEpochMilli());

        subject.process(input);

        Iterator<MockProcessorContext.CapturedForward<? extends UUID, ? extends Event>> forwarded
                = context.forwarded().iterator();
        Record<? extends UUID, ? extends Event> output = forwarded.next().record();

        assertEquals(output.key(), myUUID); // The key should be the same
        assertEquals(event, output.value()); // the event should be unwrapped

        assertFalse(forwarded.hasNext()); // Should be nothing left forwarded
    }
}