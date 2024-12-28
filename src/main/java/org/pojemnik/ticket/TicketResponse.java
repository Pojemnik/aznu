package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TicketResponse
{
    @JsonProperty
    private String event;
    @JsonProperty
    private String status;
}
