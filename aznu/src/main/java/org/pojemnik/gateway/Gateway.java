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
import org.pojemnik.payment.PaymentException;
import org.pojemnik.payment.PaymentRequest;
import org.pojemnik.payment.PaymentResponse;
import org.pojemnik.state.IncorrectStateException;
import org.pojemnik.state.StateMachine;
import org.pojemnik.ticket.TicketBookingService;
import org.pojemnik.ticket.TicketRequest;
import org.pojemnik.ticket.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.support.builder.PredicateBuilder.and;

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
    @Autowired
    private GatewayFlowService gatewayFlowService;

    private enum ServiceType
    {
        Ticket,
        Payment
    }

    private final Map<ServiceType, Map<String, StateMachine>> state = new HashMap<>();
    private final Set<String> confirmedTickets = new HashSet<>();

    @Override
    public void configure() throws Exception
    {
        initState();

        ticketExceptionHandler();
        paymentExceptionHandler();
        configureRest();
        configureTicket();
        configurePayment();

        from("direct:BookTicket").routeId("BookTicket")
                .log("brokerTopic fired").process(exchange -> {
                    exchange.getMessage().setHeader("Id", IdService.newId());
                    exchange.getMessage().setHeader("operation", "bookTicket");
                })
                .marshal().json()
                .to("kafka:BookTicket?brokers=localhost:9092");

        from("kafka:BookTicketResponse?brokers=localhost:9092").routeId("BookTicketResponseKafka")
                .choice()
                .when(and(header("Confirmed").isEqualTo(true), header("serviceType").isEqualTo("ticket")))
                .to("direct:Cleanup")
                .to("direct:Confirmed")
                .otherwise()
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    String serviceType = exchange.getMessage().getHeader("serviceType", String.class);
                    log.info("Processing response. Id: %s, serviceType: %s".formatted(id, serviceType));
                    boolean finished = false;
                    if (serviceType.equals("ticket"))
                    {
                        finished = gatewayFlowService.onTicketReserved(id);
                    }
                    else if (serviceType.equals("payment"))
                    {
                        finished = gatewayFlowService.onPaymentProcessed(id);
                    }
                    if (finished)
                    {
                        exchange.getMessage().setHeader("Finished", true);
                    }
                })
                .choice()
                .when(header("Finished").isEqualTo(true))
                .to("kafka:BookTicketConfirm?brokers=localhost:9092")
                .endChoice()
                .endChoice();

        from("direct:Cleanup").routeId("Cleanup")
                .log("Cleaning up")
                .to("kafka:BookTicketCleanup?brokers=localhost:9092");

        from("direct:Confirmed").routeId("Confirmed")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    log.info("Ticket confirmed. Id: %s".formatted(id));
                    confirmedTickets.add(id);
                });
    }

    private void initState()
    {
        for (ServiceType type : ServiceType.values())
        {
            state.put(type, new HashMap<>());
        }
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
                .responseMessage().code(200).message("Ticket booking started").endResponseMessage()
                .to("direct:BookTicket");

/*        rest("/ticket").description("Ticket booking result")
                .consumes("application/json")
                .produces("application/json")
                .get("/result/{id}").description("Get ticket booking result").outType(TicketResponse.class)
                .responseMessage().code(200).message("Ticket booking result").endResponseMessage();*/
    }

    private void configureTicket()
    {
        from("kafka:BookTicketConfirm?brokers=localhost:9092").routeId("BookTicketConfirmKafka")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    log.info("Confirming tickets for id: %s".formatted(id));
                    ticketBookingService.confirmTickets(id);
                    exchange.getMessage().setHeader("Confirmed", true);
                })
                .setHeader("serviceType", constant("ticket"))
                .to("kafka:BookTicketResponse?brokers=localhost:9092");

        from("kafka:BookTicket?brokers=localhost:9092").routeId("BookTicketKafka")
                .log("kafka ticket fired")
                .unmarshal().json(TicketRequest.class)
                .process(
                        exchange -> {
                            String id = exchange.getMessage().getHeader("Id", String.class);
                            synchronized (state.get(ServiceType.Ticket))
                            {
                                StateMachine machine = state.get(ServiceType.Ticket).computeIfAbsent(id, _ -> new StateMachine());
                                try
                                {
                                    machine.start();
                                } catch (IncorrectStateException e)
                                {
                                    //No compensation, nothing happened yet
                                    exchange.getMessage().setHeader("Compensate", false);
                                    machine.compensateOrCancel();
                                    return;
                                }
                            }
                            TicketRequest request = exchange.getMessage().getBody(TicketRequest.class);
                            ticketBookingService.bookTickets(id, request.eventId(), request.ticketsCount());
                            synchronized (state.get(ServiceType.Ticket))
                            {
                                StateMachine machine = state.get(ServiceType.Ticket).get(id);
                                try
                                {
                                    machine.finish();
                                } catch (IncorrectStateException e)
                                {
                                    exchange.getMessage().setHeader("Compensate", true);
                                    return;
                                }
                            }
                            exchange.getMessage().setHeader("Compensate", false);
                        }
                )
                .marshal().json()
                .setHeader("serviceType", constant("ticket"))
                .choice()
                .when(header("Compensate").isEqualTo(true))
                .log("Compensate is true, direct to TicketCompensation")
                .to("direct:TicketCompensation")
                .otherwise()
                .to("kafka:BookTicketResponse?brokers=localhost:9092")
                .endChoice();

        from("kafka:BookTicketError?brokers=localhost:9092").routeId("BookTicketErrorKafka")
                .unmarshal().json(ErrorInfo.class)
                .choice()
                .when(header("serviceType").isNotEqualTo("ticket"))
                .log("Direct to TicketCompensation from TicketError")
                .to("direct:TicketCompensation")
                .endChoice();

        from("direct:TicketCompensation")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    StateMachine.State lastState;
                    synchronized (state.get(ServiceType.Ticket))
                    {
                        StateMachine machine = state.get(ServiceType.Ticket).computeIfAbsent(id, _ -> new StateMachine());
                        lastState = machine.error();
                    }
                    log.info("Ticket compensation. Id: %s, last state: %s".formatted(id, lastState));
                    if (lastState == StateMachine.State.Success || lastState == StateMachine.State.Error)
                    {
                        log.info("Cancelling tickets");
                        ticketBookingService.cancelTickets(id);
                        synchronized (state.get(ServiceType.Ticket))
                        {
                            StateMachine machine = state.get(ServiceType.Ticket).get(id);
                            machine.compensateOrCancel();
                        }
                    }
                })
                .to("stream:out");

        from("kafka:BookTicketCleanup?brokers=localhost:9092").routeId("BookTicketCleanupKafka")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    synchronized (state.get(ServiceType.Ticket))
                    {
                        state.get(ServiceType.Ticket).remove(id);
                    }
                });
    }

    private void ticketExceptionHandler()
    {
        onException(EventException.class)
                .process((exchange) -> {
                            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            ErrorInfo info = new ErrorInfo(cause.toString(), cause.getMessage());
                            exchange.getMessage().setBody(info);
                            log.info("Ticket exception", cause);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("ticket"))
                .to("kafka:BookTicketError?brokers=localhost:9092")
                .handled(true);
    }

    private void configurePayment()
    {
        from("kafka:BookTicket?brokers=localhost:9092").routeId("PaymentKafka")
                .log("kafka payment fired")
                .unmarshal().json(TicketRequest.class)
                .process(
                        exchange -> {
                            String id = exchange.getMessage().getHeader("Id", String.class);
                            synchronized (state.get(ServiceType.Payment))
                            {
                                StateMachine machine = state.get(ServiceType.Payment).computeIfAbsent(id, _ -> new StateMachine());
                                try
                                {
                                    machine.start();
                                } catch (IncorrectStateException e)
                                {
                                    //No compensation, nothing happened yet
                                    exchange.getMessage().setHeader("Compensate", false);
                                    machine.compensateOrCancel();
                                    return;
                                }
                            }
                            TicketRequest ticketRequest = exchange.getMessage().getBody(TicketRequest.class);
                            PaymentRequest paymentRequest = createPaymentRequest(id, ticketRequest);
                            PaymentResponse response = paymentClientService.doPayment(paymentRequest);
                            synchronized (state.get(ServiceType.Payment))
                            {
                                StateMachine machine = state.get(ServiceType.Payment).get(id);
                                try
                                {
                                    machine.finish();
                                } catch (IncorrectStateException e)
                                {
                                    exchange.getMessage().setHeader("Compensate", true);
                                    return;
                                }
                            }
                            exchange.getMessage().setBody(response);
                            exchange.getMessage().setHeader("Compensate", false);
                        }
                )
                .marshal().json()
                .setHeader("serviceType", constant("payment"))
                .choice()
                .when(header("Compensate").isEqualTo(true))
                .log("Compensate is true, direct to PaymentCompensation")
                .to("direct:PaymentCompensation")
                .otherwise()
                .to("kafka:BookTicketResponse?brokers=localhost:9092");

        from("direct:PaymentCompensation")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    StateMachine.State lastState;
                    synchronized (state.get(ServiceType.Payment))
                    {
                        StateMachine machine = state.get(ServiceType.Payment).computeIfAbsent(id, _ -> new StateMachine());
                        lastState = machine.error();
                    }
                    log.info("Payment compensation. Id: %s, last state: %s".formatted(id, lastState));
                    if (lastState == StateMachine.State.Success || lastState == StateMachine.State.Error)
                    {
                        log.info("Cancelling payment");
                        paymentClientService.compensatePayment(id);
                        synchronized (state.get(ServiceType.Payment))
                        {
                            StateMachine machine = state.get(ServiceType.Payment).get(id);
                            machine.compensateOrCancel();
                        }
                    }
                })
                .to("stream:out");

        from("kafka:BookTicketError?brokers=localhost:9092").routeId("PaymentErrorKafka")
                .unmarshal().json(ErrorInfo.class)
                .log("Error received by payment handler")
                .choice()
                .when(header("serviceType").isNotEqualTo("payment"))
                .log("Direct to PaymentCompensation from PaymentError")
                .to("direct:PaymentCompensation")
                .endChoice();

        from("kafka:BookTicketCleanup?brokers=localhost:9092").routeId("PaymentCleanupKafka")
                .process(exchange -> {
                    String id = exchange.getMessage().getHeader("Id", String.class);
                    synchronized (state.get(ServiceType.Payment))
                    {
                        state.get(ServiceType.Payment).remove(id);
                    }
                });
    }

    private void paymentExceptionHandler()
    {
        onException(PaymentException.class)
                .process((exchange) -> {
                            Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            ErrorInfo info = new ErrorInfo(cause.toString(), cause.getMessage());
                            exchange.getMessage().setBody(info);
                            log.info("Payment exception", cause);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("payment"))
                .to("kafka:BookTicketError?brokers=localhost:9092")
                .handled(true);
    }

    private static PaymentRequest createPaymentRequest(String transactionId, TicketRequest ticketRequest)
    {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCost(100);//TODO: get from ticket info
        paymentRequest.setCreditCardNumber(ticketRequest.creditCardNumber());
        paymentRequest.setCreditCardCvv(ticketRequest.creditCardCvv());
        paymentRequest.setCreditCardOwner(ticketRequest.creditCardOwner());
        paymentRequest.setCreditCardExpirationDate(ticketRequest.creditCardExpirationDate());
        paymentRequest.setTransactionId(transactionId);
        ;
        return paymentRequest;
    }

}
