package com.sandbox.runtime.models.jms;


import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.ServiceScriptException;

import javax.script.ScriptEngine;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class JMSRequest extends EngineRequest {

    private final String destination;

    public JMSRequest(ScriptEngine scriptEngine, String destination, Map<String, String> headers, Map<String, String> properties, Object body, String contentType, String ip) throws ServiceScriptException {
        super(scriptEngine, headers, properties, body, contentType, ip);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

}