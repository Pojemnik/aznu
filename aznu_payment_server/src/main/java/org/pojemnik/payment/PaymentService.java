package org.pojemnik.payment;

import jakarta.annotation.PostConstruct;
import jakarta.jws.WebService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@WebService
@Log
public final class PaymentService
{
    private final Pattern cvvPattern = Pattern.compile("^[0-9]{3}$");

    public PaymentResponse processPayment(PaymentRequest request)
    {
        log.info("Processing payment for transaction %s".formatted(request.getTransactionId()));
        String result = getResult(request);
        try
        {
            Thread.sleep(3000);
        } catch (InterruptedException ignored)
        {

        }
        log.info("Result for transaction %s is %s".formatted(request.getTransactionId(), result));
        return new PaymentResponse(result);
    }

    public String cancelPayment(String transactionId)
    {
        return "ok";
    }

    private String getResult(PaymentRequest request)
    {
        if (request.getCost() <= 0)
        {
            return "Cost not positive";
        }
        if (!cvvPattern.matcher(request.getCreditCardCvv()).find())
        {
            return "CVV invalid";
        }
        return "ok";
    }
}
