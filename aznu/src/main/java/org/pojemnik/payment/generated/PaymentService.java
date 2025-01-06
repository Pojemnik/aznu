package org.pojemnik.payment.generated;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 4.0.6
 * 2025-01-04T09:58:43.255+01:00
 * Generated source version: 4.0.6
 *
 */
@WebService(targetNamespace = "http://payment.pojemnik.org/", name = "PaymentService")
@XmlSeeAlso({ObjectFactory.class})
public interface PaymentService {

    @WebMethod
    @RequestWrapper(localName = "processPayment", targetNamespace = "http://payment.pojemnik.org/", className = "org.pojemnik.payment.generated.ProcessPayment")
    @ResponseWrapper(localName = "processPaymentResponse", targetNamespace = "http://payment.pojemnik.org/", className = "org.pojemnik.payment.generated.ProcessPaymentResponse")
    @WebResult(name = "return", targetNamespace = "")
    public PaymentResponse processPayment(

        @WebParam(name = "arg0", targetNamespace = "")
        PaymentRequest arg0
    );

    @WebMethod
    @RequestWrapper(localName = "cancelPayment", targetNamespace = "http://payment.pojemnik.org/", className = "org.pojemnik.payment.generated.CancelPayment")
    @ResponseWrapper(localName = "cancelPaymentResponse", targetNamespace = "http://payment.pojemnik.org/", className = "org.pojemnik.payment.generated.CancelPaymentResponse")
    @WebResult(name = "return", targetNamespace = "")
    public java.lang.String cancelPayment(

        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
}