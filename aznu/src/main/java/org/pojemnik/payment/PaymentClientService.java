package org.pojemnik.payment;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.net.URL;

@Service
public class PaymentClientService
{
    private static final QName SERVICE_NAME = new QName("http://payment.pojemnik.org/", "PaymentServiceService");
    private org.pojemnik.payment.PaymentService port;

    @PostConstruct
    private void init()
    {
        URL wsdlURL = org.pojemnik.payment.PaymentServiceService.WSDL_LOCATION;

        org.pojemnik.payment.PaymentServiceService ss = new org.pojemnik.payment.PaymentServiceService(wsdlURL, SERVICE_NAME);
        port = ss.getPaymentServicePort();
    }

    public org.pojemnik.payment.PaymentResponse doPayment(org.pojemnik.payment.PaymentRequest request)
    {
        System.out.println("Invoking processPayment");
        org.pojemnik.payment.PaymentResponse result = port.processPayment(request);
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
