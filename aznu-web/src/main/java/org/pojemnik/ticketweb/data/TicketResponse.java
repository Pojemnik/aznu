package org.pojemnik.ticketweb.data;

import com.fasterxml.jackson.annotation.JsonProperty;


public record TicketResponse(@JsonProperty String status, @JsonProperty String errorMessage)
{
}
