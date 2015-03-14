package com.sandbox.runtime.models.jms;

import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.RuntimeResponse;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.Collection;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class JMSResponse extends EngineResponse {

    private String responseDestination;

    // contentType defaulted to 'application/json'
    public void send(Object body) {
        if (body instanceof ScriptObject || body instanceof Map || body instanceof Collection) {
            // if contentType not already set then do it.
            if (!getHeaders().containsKey("contentType"))
                getHeaders().put("contentType", "application/json");
        } else {
            // treat everything else as plain text
            if (!getHeaders().containsKey("contentType"))
                getHeaders().put("contentType", "text/plain");
        }

        setBody(body);

        // set route matched
        setRendered(false);
    }

    public void send(String destination, Object body) {
        this.responseDestination = destination;
        this.send(body);
    }

    public void send(NativeArray body) {
        // if contentType not already set then do it.
        if (!getHeaders().containsKey("contentType"))
            getHeaders().put("contentType", "application/json");

        setBody(body);

        setRendered(false);
    }

    public void send(String destination, NativeArray body) {
        this.responseDestination = destination;
        this.send(body);
    }

    /**
     * Always set contentType header to application/json. Convert the JS object to string
     * @param body
     */
    public void json(String destination, Object body) {
        this.responseDestination = destination;
        this.json(body);
    }

    public void json(Object body) {
        getHeaders().put("contentType", "application/json");
        this.send(body);
    }

    @Override
    public RuntimeResponse _getRuntimeResponse(EngineRequest req, String body) throws Exception {
        return new JMSRuntimeResponse(body, getHeaders(), req.getHeaders(), responseDestination);
    }

}