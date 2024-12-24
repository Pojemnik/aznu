package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TicketRequest
{
    @JsonProperty
    private String event;
}
