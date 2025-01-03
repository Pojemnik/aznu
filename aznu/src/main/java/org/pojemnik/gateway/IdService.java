package org.pojemnik.gateway;

import java.util.UUID;

public class IdService
{
    public static String newId()
    {
        return UUID.randomUUID().toString();
    }
}
