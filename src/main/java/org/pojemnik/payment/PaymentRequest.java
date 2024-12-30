package org.pojemnik.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentRequest(@JsonProperty String creditCardNumber,
                             @JsonProperty String creditCardOwner,
                             @JsonProperty String creditCardExpirationDate,
                             @JsonProperty String creditCardCvv,
                             @JsonProperty double cost)
{

}
