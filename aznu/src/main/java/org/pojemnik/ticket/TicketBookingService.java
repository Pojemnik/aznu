package org.pojemnik.ticket;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.pojemnik.event.*;
import org.pojemnik.event.exceptions.EventConfirmationException;
import org.pojemnik.event.exceptions.EventException;
import org.pojemnik.event.exceptions.IncorrectEventException;
import org.pojemnik.event.exceptions.NoTicketsAvailableException;
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

    @Data
    @AllArgsConstructor
    private class TicketInfo
    {
        private int eventId;
        private int ticketsCount;
    }

    private final Map<String, TicketInfo> processedTickets = new HashMap<>();

    public void bookTickets(String transactionId, int eventId, int count) throws EventException
    {
        Event event = eventRepository.getEvent(eventId);
        if (event == null)
        {
            throw new IncorrectEventException("Event with id %d does not exist".formatted(eventId));
        }
        if (event.getTime().isBefore(LocalDateTime.now()))
        {
            throw new NoTicketsAvailableException("Event with id %d has already passed".formatted(eventId));
        }
        synchronized (event)
        {
            if (event.getAvailableTickets() < count)
            {
                throw new NoTicketsAvailableException("Not enough tickets available for event with id %d".formatted(eventId));
            }
            event.setAvailableTickets(event.getAvailableTickets() - count);
            TicketInfo info = new TicketInfo(eventId, count);
            processedTickets.put(transactionId, info);
        }
    }

    public void confirmTickets(String transactionId) throws IncorrectEventException
    {
        if (!processedTickets.containsKey(transactionId))
        {
            throw new EventConfirmationException("No tickets were processed in transaction %s".formatted(transactionId));
        }
        processedTickets.remove(transactionId);
    }

    public void cancelTickets(String transactionId) throws IncorrectEventException
    {
        if (!processedTickets.containsKey(transactionId))
        {
            throw new EventConfirmationException("No tickets were processed in transaction %s".formatted(transactionId));
        }
        TicketInfo info = processedTickets.get(transactionId);
        Event event = eventRepository.getEvent(info.eventId);
        synchronized (event)
        {
            event.setAvailableTickets(event.getAvailableTickets() + info.ticketsCount);
            processedTickets.remove(transactionId);
        }
    }
}
