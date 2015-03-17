package com.sandbox.runtime.models.jms;


import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.ServiceScriptException;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class JMSRequest extends EngineRequest {

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    private final String destination;

    public JMSRequest(ScriptEngine scriptEngine, String destination, Map<String, String> headers, Map<String, String> properties, Object body, String contentType, String ip) throws ServiceScriptException {
        super(scriptEngine, headers, properties, body, contentType, ip);
        this.destination = destination;
    }

    public String destination() {
        return destination;
    }

    @Override
    public boolean is(String type){
        return super.is(type, "contentType");
    }

    @Override
    public Exception _getNoRouteDefinitionException() {
        return new ServiceScriptException("Could not find a route definition matching your requested route " + destination());
    }

    @Override
    public RuntimeResponse _getErrorResponse(Error error) {
        return new JMSRuntimeResponse(error);
    }

    @Override
    public EngineResponse _getMatchingResponse() {
        JMSResponse response = new JMSResponse();
        //if we have a correlation ID set it in the response
        if(getHeaders().containsKey("JMSCorrelationID")) response.getHeaders().put("JMSCorrelationID", getHeaders().get("JMSCorrelationID"));
        response.getHeaders().put("JMSDeliveryMode","NON_PERSISTENT");

        return response;
    }
}