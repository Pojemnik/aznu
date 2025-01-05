package org.pojemnik.ticketweb.controllers;

import org.pojemnik.ticketweb.data.TicketRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TicketController
{
    @GetMapping("/ticket")
    public String ticketForm(Model model)
    {
        model.addAttribute("ticketRequest", new TicketRequest());
        return "ticket";
    }

    @PostMapping("/ticket")
    public String bookTicket(TicketRequest ticket)
    {
        //TODO: implement
        return "";
    }

}
