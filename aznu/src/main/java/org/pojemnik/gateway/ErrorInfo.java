package org.pojemnik.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorInfo
{
    private final String error;
    private final String message;
}
