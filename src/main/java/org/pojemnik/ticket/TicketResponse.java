package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


public record TicketResponse(@JsonProperty int eventId, @JsonProperty String status)
{
}
