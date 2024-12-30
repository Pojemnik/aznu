package org.pojemnik.payment;

import jakarta.jws.WebService;
import org.springframework.stereotype.Service;

@Service
@WebService
public class PaymentService
{
    public PaymentResponse processPayment(PaymentRequest request)
    {
        return new PaymentResponse("ok");
    }
}
