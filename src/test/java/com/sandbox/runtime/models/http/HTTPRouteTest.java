package com.sandbox.runtime.models.http;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nickhoughton on 25/01/2017.
 */
public class HTTPRouteTest {

    HTTPRoute simpleGet;
    HTTPRoute paramGet;
    HTTPRoute postWithProps;

    @Before
    public void setUp() throws Exception {
        simpleGet = new HTTPRoute("GET", "/1/hello", Collections.emptyMap());
        paramGet = new HTTPRoute("GET", "/1/hello/{name}", Collections.emptyMap());

        Map<String, String> properties = new HashMap<>();
        properties.put("SOAPAction", "doStuff");
        postWithProps = new HTTPRoute("POST", "/soap-endpoint", properties);

    }

    @Test
    public void testMatchesMethod() throws Exception {
        assertTrue(simpleGet.matchesMethod("*"));
        assertTrue(simpleGet.matchesMethod("all"));
        assertTrue(simpleGet.matchesMethod("ALL"));
        assertFalse(simpleGet.matchesMethod("blah"));

        assertTrue(simpleGet.matchesMethod("GET"));
        assertTrue(simpleGet.matchesMethod("get"));
        assertFalse(simpleGet.matchesMethod(null));
    }

    @Test
    public void testMatchesExactPath() throws Exception {
        assertTrue(simpleGet.matchesExactPath("/1/hello"));
        assertFalse(simpleGet.matchesExactPath("/something-else"));
    }

    @Test
    public void testMatchesProperties() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("a", "b");

        assertFalse(postWithProps.matchesProperties(properties));

        properties = new HashMap<>();
        properties.put("SOAPAction", "doStuff");
        assertTrue(postWithProps.matchesProperties(properties));

        properties = new HashMap<>();
        properties.put("SOAPAction", "doStuff");
        properties.put("a", "b");
        assertTrue(postWithProps.matchesProperties(properties));

    }

    @Test
    public void testIsMatch() throws Exception {
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/1/hello");
        assertTrue(simpleGet.isMatch(request));

        request = new HttpRuntimeRequest();
        request.setMethod("POST");
        request.setUrl("/1/hello");
        assertFalse(simpleGet.isMatch(request));

        request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/1/hello/meep");
        assertTrue(paramGet.isMatch(request));
    }

    @Test
    public void testIsMatch1() throws Exception {
        assertTrue(simpleGet.isMatch(simpleGet));
        assertFalse(simpleGet.isMatch(paramGet));
    }

    @Test
    public void testIsMatch2() throws Exception {
        assertTrue(simpleGet.isMatch("get", "/1/hello", Collections.emptyMap()));

        MultivaluedMap<String, String> urlParams = new MultivaluedHashMap<>();
        assertTrue(paramGet.isMatch("get", "/1/hello/blah", urlParams, Collections.emptyMap()));
        assertEquals(1, urlParams.get("name").size());
    }

    @Test
    public void testIsUncompiledMatch() throws Exception {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.method()).thenReturn("GET");

        when(request.path()).thenReturn("/1/hello");
        when(request.getPath()).thenReturn("/1/hello");
        assertTrue(simpleGet.isUncompiledMatch(request));

        request = mock(HTTPRequest.class);
        when(request.getMethod()).thenReturn("get");
        when(request.method()).thenReturn("get");

        when(request.path()).thenReturn("/1/hello");
        when(request.getPath()).thenReturn("/1/hello");
        assertTrue(simpleGet.isUncompiledMatch(request));

        request = mock(HTTPRequest.class);
        when(request.getMethod()).thenReturn("post");
        when(request.method()).thenReturn("post");

        when(request.path()).thenReturn("/1/junk");
        when(request.getPath()).thenReturn("/1/junk");
        assertFalse(simpleGet.isUncompiledMatch(request));
    }

    @Test
    public void testIsUncompiledMatch1() throws Exception {

    }
}