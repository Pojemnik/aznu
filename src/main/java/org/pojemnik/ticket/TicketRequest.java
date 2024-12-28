package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketRequest(@JsonProperty int eventId, @JsonProperty int ticketsCount)
{
    public TicketRequest
    {
        if (ticketsCount <= 0)
        {
            throw new IllegalArgumentException("Tickets count must be a positive integer");
        }
    }
}
