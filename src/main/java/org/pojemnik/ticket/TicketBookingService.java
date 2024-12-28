package org.pojemnik.ticket;


import org.pojemnik.event.Event;
import org.pojemnik.event.EventRepository;
import org.pojemnik.event.IncorrectEventException;
import org.pojemnik.event.NoTicketsAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicketBookingService
{
    @Autowired
    private EventRepository eventRepository;

    public void bookTickets(int id, int count) throws IncorrectEventException, NoTicketsAvailableException
    {
        Event event = eventRepository.getEvent(id);
        if (event == null)
        {
            throw new IncorrectEventException("Event with id %d does not exist".formatted(id));
        }
        if (event.getAvailableTickets() < count)
        {
            throw new NoTicketsAvailableException("Not enough tickets available for event with id %d".formatted(id));
        }
        if (event.getTime().isBefore(LocalDateTime.now()))
        {
            throw new NoTicketsAvailableException("Event with id %d has already passed".formatted(id));
        }
        event.setAvailableTickets(event.getAvailableTickets() - count);
    }
}
