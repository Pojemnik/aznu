package org.pojemnik.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketRequest(
        @JsonProperty int eventId,
        @JsonProperty int ticketsCount,
        @JsonProperty String creditCardNumber,
        @JsonProperty String creditCardOwner,
        @JsonProperty String creditCardExpirationDate,
        @JsonProperty String creditCardCvv
)
{
}
