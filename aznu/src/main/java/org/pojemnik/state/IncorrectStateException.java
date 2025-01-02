package org.pojemnik.state;

public class IncorrectStateException extends Exception
{
    public IncorrectStateException(String message)
    {
        super(message);
    }
}
