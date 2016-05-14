package com.sandbox.runtime.models.jms;


import com.sandbox.common.models.jms.JMSRuntimeResponse;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.common.models.Error;
import com.sandbox.common.models.RuntimeResponse;
import com.sandbox.common.models.ServiceScriptException;
import com.sandbox.runtime.models.XMLDoc;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class JMSRequest extends EngineRequest {

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    private final String destination;

    public JMSRequest(ScriptEngine scriptEngine, String destination, Map<String, String> headers, Map<String, String> properties, Object body, String contentType, String ip) throws Exception {
        super(scriptEngine, headers, properties, body, contentType, ip);
        this.destination = destination;
    }

    @Override
    public ScriptObject headers() {
        return super.headers();
    }

    @Override
    public ScriptObject getHeaders() {
        return super.getHeaders();
    }

    public String destination() {
        return destination;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public boolean is(String type){
        return super.is(type, "contentType");
    }

    public Object get(String headerName){
        return super.get(headerName);
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

    private static List<String> accessibleProperties = new ArrayList();
    public List<String> _getAccessibleProperties() {
        return super._getAccessibleProperties(accessibleProperties, JMSRequest.class);
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