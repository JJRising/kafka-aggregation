package com.nuvalence.aggregation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Event {

    public enum Type {
        INITIALIZING,
        CONTINUING,
        TERMINATING
    }

    @JsonProperty
    private UUID id;

    @JsonProperty
    private Type type;

    @JsonProperty
    private String message;
}
