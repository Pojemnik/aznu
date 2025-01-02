package org.pojemnik.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class Event
{
    @JsonProperty
    private final int id;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String location;
    @JsonProperty
    private final LocalDateTime time;
    @JsonProperty
    private int availableTickets;
    private final int maxTickets;

    public Event(int id, String name, String location, LocalDateTime time, int maxTickets)
    {
        this.id = id;
        this.name = name;
        this.location = location;
        this.time = time;
        this.availableTickets = maxTickets;
        this.maxTickets = maxTickets;
    }
}
