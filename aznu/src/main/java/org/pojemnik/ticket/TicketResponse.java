package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;


public record TicketResponse(@JsonProperty int eventId, @JsonProperty String status)
{
}
