package org.pojemnik.ticketweb.controllers;

import org.pojemnik.ticketweb.data.TicketRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
        String id = restTemplate.postForObject("http://gateway:8080/api/ticket/book", ticket, String.class);
        model.addAttribute("id", id);
        return "ticketResult";
    }
}
