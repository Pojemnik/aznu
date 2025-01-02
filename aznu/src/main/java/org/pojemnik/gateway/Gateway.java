/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pojemnik.gateway;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.pojemnik.event.exceptions.EventException;
import org.pojemnik.payment.PaymentClientService;
import org.pojemnik.payment.PaymentRequest;
import org.pojemnik.payment.PaymentResponse;
import org.pojemnik.ticket.TicketBookingService;
import org.pojemnik.ticket.TicketRequest;
import org.pojemnik.ticket.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.apache.camel.model.rest.RestParamType.body;

@Component
public class Gateway extends RouteBuilder
{
    @Autowired
    private Environment env;

    @Value("${camel.servlet.mapping.context-path}")
    private String contextPath;

    @Autowired
    private TicketBookingService ticketBookingService;
    @Autowired
    private PaymentClientService paymentClientService;

    @Override
    public void configure() throws Exception
    {
        configureRest();
        configureExceptionHandlers();
        configureTicket();
        configurePayment();

        from("direct:BookTicket").routeId("BookTicket")
                .log("brokerTopic fired")
                .marshal().json()
                .to("kafka:BookTicket?brokers=localhost:9092");

        from("kafka:BookTicketError?brokers=localhost:9092").routeId("BookTicketErrorKafka")
                //TODO: update states, cleanup
                .log("error sent")
                .to("direct:notification");

        from("kafka:BookTicketResponse?brokers=localhost:9092").routeId("BookTicketResponseKafka")
                //TODO: update states
                .log("notification sent")
                .to("direct:notification");

        from("direct:notification").routeId("notification")
                .to("stream:out");
    }

    private void configureRest()
    {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .port(env.getProperty("server.port", "8080"))
                .contextPath(contextPath.substring(0, contextPath.length() - 2))
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Ticket booking API")
                .apiProperty("api.version", "1.0.0");

        rest("/ticket").description("Ticket booking")
                .consumes("application/json")
                .produces("application/json")
                .post("/book").description("Book a ticket").type(TicketRequest.class)
                .param().name("body").type(body).description("The ticket to book").endParam()
                .responseMessage().code(200).message("Ticket successfully booked").endResponseMessage()
                .to("direct:BookTicket");
    }

    private void configureExceptionHandlers()
    {
        onException(EventException.class)
                .process((exchange) -> {
                            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            ErrorInfo info = new ErrorInfo(cause.toString(), cause.getMessage());
                            exchange.getMessage().setBody(info);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("ticket"))
                .to("kafka:BookTicketError?brokers=localhost:9092")
                .handled(true);

        onException(EventException.class)
                .process((exchange) -> {
                            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            ErrorInfo info = new ErrorInfo(cause.toString(), cause.getMessage());
                            exchange.getMessage().setBody(info);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("payment"))
                .to("kafka:BookTicketError?brokers=localhost:9092")
                .handled(true);
    }

    private void configureTicket()
    {
        from("kafka:BookTicket?brokers=localhost:9092").routeId("BookTicketKafka")
                .log("kafka ticket fired")
                .unmarshal().json(TicketRequest.class)
                .process(
                        exchange -> {
                            TicketRequest request = exchange.getMessage().getBody(TicketRequest.class);
                            ticketBookingService.bookTickets(request.eventId(), request.ticketsCount());
                            TicketResponse response = new TicketResponse(request.eventId(), "success");
                            exchange.getMessage().setBody(response);
                        }
                )
                .marshal().json()
                .setHeader("serviceType", constant("ticket"))
                .to("kafka:BookTicketResponse?brokers=localhost:9092");
    }

    private void configurePayment()
    {
        from("kafka:BookTicket?brokers=localhost:9092").routeId("PaymentKafka")
                .log("kafka payment fired")
                .unmarshal().json(TicketRequest.class)
                .process(
                        exchange -> {
                            TicketRequest ticketRequest = exchange.getMessage().getBody(TicketRequest.class);
                            PaymentRequest paymentRequest = createPaymentRequest(ticketRequest);
                            paymentClientService.doPayment(paymentRequest);
                            PaymentResponse response = new PaymentResponse();
                            response.setResult("Success");
                            exchange.getMessage().setBody(response);
                        }
                )
                .marshal().json()
                .setHeader("serviceType", constant("payment"))
                .to("kafka:BookTicketResponse?brokers=localhost:9092");
    }

    private static PaymentRequest createPaymentRequest(TicketRequest ticketRequest)
    {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCost(100);//TODO: get from ticket info
        paymentRequest.setCreditCardNumber(ticketRequest.creditCardNumber());
        paymentRequest.setCreditCardCvv(ticketRequest.creditCardCvv());
        paymentRequest.setCreditCardOwner(ticketRequest.creditCardOwner());
        paymentRequest.setCreditCardExpirationDate(ticketRequest.creditCardExpirationDate());
        return paymentRequest;
    }

}
