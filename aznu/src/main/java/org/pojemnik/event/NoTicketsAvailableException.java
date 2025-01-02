package org.pojemnik.event;

public class NoTicketsAvailableException extends Exception
{
    public NoTicketsAvailableException(String message)
    {
        super(message);
    }
}
