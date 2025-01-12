package org.pojemnik.payment;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest
{
    private String creditCardNumber;
    private String creditCardOwner;
    private String creditCardExpirationDate;
    private String creditCardCvv;
    private double cost;
    private String transactionId;
}
