package org.pojemnik.ticket;


import org.pojemnik.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TicketBookingService
{
    @Autowired
    private EventRepository eventRepository;

    private final Map<Integer, Integer> processedTickets = new HashMap<>();

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
        processedTickets.put(id, processedTickets.getOrDefault(id, 0) + count);
    }

    public void confirmTickets(int id) throws IncorrectEventException
    {
        if (!processedTickets.containsKey(id))
        {
            throw new EventConfirmationException("No tickets were processed for event with id %d".formatted(id));
        }
        processedTickets.remove(id);
    }

    public void cancelTickets(int id) throws IncorrectEventException
    {
        if (!processedTickets.containsKey(id))
        {
            throw new EventConfirmationException("No tickets were processed for event with id %d".formatted(id));
        }
        Event event = eventRepository.getEvent(id);
        event.setAvailableTickets(event.getAvailableTickets() + processedTickets.get(id));
        processedTickets.remove(id);
    }
}
