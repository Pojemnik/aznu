package org.pojemnik.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;


public record TicketResponse(@JsonProperty String status, @JsonProperty String errorMessage)
{
}
