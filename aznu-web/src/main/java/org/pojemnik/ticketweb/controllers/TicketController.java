package org.pojemnik.ticketweb.controllers;

import org.pojemnik.ticketweb.data.TicketRequest;
import org.pojemnik.ticketweb.data.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Controller
public class TicketController
{
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/ticket")
    public String ticketForm(Model model)
    {
        model.addAttribute("ticketRequest", new TicketRequest());
        return "ticket";
    }

    @PostMapping("/ticket")
    public String bookTicket(TicketRequest ticket, Model model)
    {
        String id = restTemplate.postForObject("http://localhost:8080/api/ticket/book", ticket, String.class);
        model.addAttribute("id", id);
        return "ticketResult";
    }

    //@GetMapping("/ticketResult")
    //public String ticketResult(Model model)
    //{
    //    String id = Objects.requireNonNull(model.getAttribute("id")).toString();
    //    TicketResponse response = restTemplate.getForObject("http://localhost:8080/api/ticket/result/%s".formatted(id), TicketResponse.class);
    //    model.addAttribute("response", response);
    //    return "ticketResult";
    //}
}
