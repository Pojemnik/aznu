package org.pojemnik.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo
{
    private String error;
    private String message;

    public static ErrorInfo noError()
    {
        return new ErrorInfo("No error", "");
    }
}
