package org.pojemnik.event;

public class EventInThePastException extends RuntimeException
{
    public EventInThePastException(String message)
    {
        super(message);
    }
}
