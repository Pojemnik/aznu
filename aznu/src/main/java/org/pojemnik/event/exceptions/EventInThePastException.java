package org.pojemnik.event.exceptions;

public class EventInThePastException extends EventException
{
    public EventInThePastException(String message)
    {
        super(message);
    }
}
