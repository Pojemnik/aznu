package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TicketRequest
{
    @JsonProperty
    private String event;
}
