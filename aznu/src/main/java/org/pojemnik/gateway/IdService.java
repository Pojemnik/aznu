package org.pojemnik.gateway;

import java.util.UUID;

public class IdService
{
    public static String getId()
    {
        return UUID.randomUUID().toString();
    }
}
