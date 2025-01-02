package org.pojemnik.payment;

import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.net.URL;

@Service
public class PaymentClientService
{
    private static final QName SERVICE_NAME = new QName("http://payment.pojemnik.org/", "PaymentServiceService");

    public org.pojemnik.payment.PaymentResponse doPayment(org.pojemnik.payment.PaymentRequest request)
    {
        URL wsdlURL = org.pojemnik.payment.PaymentServiceService.WSDL_LOCATION;

        org.pojemnik.payment.PaymentServiceService ss = new org.pojemnik.payment.PaymentServiceService(wsdlURL, SERVICE_NAME);
        org.pojemnik.payment.PaymentService port = ss.getPaymentServicePort();

        System.out.println("Invoking processPayment");
        org.pojemnik.payment.PaymentResponse result = port.processPayment(request);
        System.out.println("processPayment.result = " + result.getResult());
        return result;
    }
}
