package org.pojemnik.payment;

import jakarta.annotation.PostConstruct;
import org.pojemnik.payment.generated.PaymentRequest;
import org.pojemnik.payment.generated.PaymentResponse;
import org.pojemnik.payment.generated.PaymentService;
import org.pojemnik.payment.generated.PaymentServiceService;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.net.URL;

@Service
public class PaymentClientService
{
    private static final QName SERVICE_NAME = new QName("http://payment.pojemnik.org/", "PaymentServiceService");
    private PaymentService port;

    @PostConstruct
    private void init()
    {
        URL wsdlURL = PaymentServiceService.WSDL_LOCATION;

        PaymentServiceService ss = new PaymentServiceService(wsdlURL, SERVICE_NAME);
        port = ss.getPaymentServicePort();
    }

    public PaymentResponse doPayment(PaymentRequest request)
    {
        System.out.println("Invoking processPayment");
        PaymentResponse result;
        try
        {
            result = port.processPayment(request);
        }
        catch (Exception e)
        {
            throw new PaymentException("Payment system error: %s".formatted(e.getMessage()));
        }
        System.out.println("processPayment.result = " + result.getResult());
        if (!result.getResult().equals("ok"))
        {
            throw new PaymentException("Payment system message: %s".formatted(result.getResult()));
        }
        return result;
    }

    public String compensatePayment(String paymentId)
    {
        System.out.println("Invoking compensatePayment");
        String result = port.cancelPayment(paymentId);
        System.out.println("compensatePayment.result = " + result);
        return result;
    }
}
