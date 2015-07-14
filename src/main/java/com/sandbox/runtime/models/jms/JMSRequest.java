package com.sandbox.runtime.models.jms;


import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.ServiceScriptException;
import com.sandbox.runtime.models.XMLDoc;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.util.List;
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

    public String get(String headerName){
        if(getHeaders() == null) return null;
        return getHeaders().get(headerName);
    }

    public Map<String, String> headers() {
        return super.getHeaders();
    }

    public Map<String, String> properties() {
        return super.getProperties();
    }

    public Object body() {
        return super.getBody();
    }

    public String contentType() {
        return super.getContentType();
    }

    public String ip() {
        return super.getIp();
    }

    public XMLDoc xmlDoc() {
        return super.getXmlDoc();
    }

    public String bodyAsString() {
        return super.getBodyAsString();
    }

    public List<String> _getAccessibleProperties() {
        return super._getAccessibleProperties();
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
        return new JMSResponse();
    }
}