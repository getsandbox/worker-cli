package com.sandbox.worker;

import com.sandbox.worker.models.enums.RuntimeTransportType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteSupportTest {

    @Test
    void shouldGenerateStableRouteIdForInput() {
        //stable rest/normal urls
        assertEquals("7b8c351b", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "OPTIONS","/", Collections.emptyMap()));
        assertEquals("4fdb64e7", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "PUT","/:template/path", Collections.emptyMap()));
        assertEquals("1f05789d", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "GET","/", Collections.emptyMap()));
        assertEquals("1f05789d", RouteSupport.generateRouteIdentifier("http", "Get","/", Collections.emptyMap()));
        assertEquals("1f05789d", RouteSupport.generateRouteIdentifier("Http", "get","/", Collections.emptyMap()));
        assertEquals("8e256814", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "GET","/path/with/values", Collections.emptyMap()));
        assertEquals("638723c1", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "GET","/path/with/values/", Collections.emptyMap()));

        //soap with action
        Map<String, String> properties = new HashMap<>();
        properties.put("SOAPAction","action-1");
        assertEquals("17898a4e", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "POST","/soapThing", properties));
        properties = new HashMap<>();
        properties.put("SOAPAction","action-2");
        assertEquals("8e80dbf4", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "POST","/soapThing", properties));

        //soap with action and operation
        properties = new HashMap<>();
        properties.put("SOAPAction","generic");
        properties.put("SOAPOperationName","doSomething");
        assertEquals("6496b8ee", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "POST","/soapService", properties));
        properties = new HashMap<>();
        properties.put("SOAPAction","generic");
        properties.put("SOAPOperationName","doSomethingElse");
        assertEquals("65a0ebcb", RouteSupport.generateRouteIdentifier(RuntimeTransportType.HTTP.name(), "POST","/soapService", properties));
    }
}