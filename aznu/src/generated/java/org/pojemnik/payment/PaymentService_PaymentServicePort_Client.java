
package org.pojemnik.payment;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 4.0.6
 * 2025-01-02T15:02:48.560+01:00
 * Generated source version: 4.0.6
 *
 */
public final class PaymentService_PaymentServicePort_Client {

    private static final QName SERVICE_NAME = new QName("http://payment.pojemnik.org/", "PaymentServiceService");

    private PaymentService_PaymentServicePort_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = PaymentServiceService.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) {
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        PaymentServiceService ss = new PaymentServiceService(wsdlURL, SERVICE_NAME);
        PaymentService port = ss.getPaymentServicePort();

        {
        System.out.println("Invoking processPayment...");
        org.pojemnik.payment.PaymentRequest _processPayment_arg0 = new org.pojemnik.payment.PaymentRequest();
        org.pojemnik.payment.PaymentResponse _processPayment__return = port.processPayment(_processPayment_arg0);
        System.out.println("processPayment.result=" + _processPayment__return);


        }

        System.exit(0);
    }

}
