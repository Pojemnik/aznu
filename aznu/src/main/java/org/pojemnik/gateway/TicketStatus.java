package org.pojemnik.gateway;

public record TicketStatus(Status status, ErrorInfo errorInfo)
{
        public enum Status
    {
        Ok, Error, Processing
    }
}
