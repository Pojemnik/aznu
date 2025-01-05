package org.pojemnik.event;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class EventRepository
{
    private final Map<Integer, Event> events = Map.of(
            0, new Event(0, "New Year Concert", "Concert Hall", LocalDateTime.of(2024, 12, 31, 23, 0), 100),
            1, new Event(1, "Rock Festival", "Open Air", LocalDateTime.of(2025, 8, 1, 12, 0), 1000),
            2, new Event(2, "Theatre Play", "Downtown", LocalDateTime.of(2025, 3, 5, 19, 0), 1),
            3, new Event(3, "Cinema Night", "Cinema", LocalDateTime.of(2024, 6, 1, 20, 0), 200)
    );

    public List<Event> getEvents()
    {
        return List.copyOf(events.values());
    }

    public Event getEvent(int id)
    {
        return events.get(id);
    }
}
