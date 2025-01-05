package org.pojemnik.ticketweb.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketRequest
{
    @JsonProperty
    private int eventId;
    @JsonProperty
    private int ticketsCount;
    @JsonProperty
    private String creditCardNumber;
    @JsonProperty
    private String creditCardOwner;
    @JsonProperty
    private String creditCardExpirationDate;
    @JsonProperty
    private String creditCardCvv;
}
