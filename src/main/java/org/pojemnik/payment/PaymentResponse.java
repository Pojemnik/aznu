package org.pojemnik.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentResponse(@JsonProperty String result)
{
}
