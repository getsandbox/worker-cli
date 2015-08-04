package com.sandbox.runtime.models.jms;

import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.EngineResponseMessage;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.ServiceScriptException;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.Collection;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class JMSResponse extends EngineResponse {

    public void send(Object obj) throws ServiceScriptException {
        throw new ServiceScriptException("Invalid send() call, JMS send() must have 2 parameters, send(queueName, content)");
    }

    // contentType defaulted to 'application/json'
    public void send(String destination, Object body) {
        getActiveMessage().setResponseDestination(destination);

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

        //complete current message, get ready to start a new one..
        completeActiveMessage();
    }

    public void json(Object obj) throws ServiceScriptException {
        throw new ServiceScriptException("Invalid send() call, JMS json() must have 2 parameters, json(queueName, content)");
    }

    /**
     * Always set contentType header to application/json. Convert the JS object to string
     * @param body
     */
    public void json(String destination, Object body) {
        getHeaders().put("contentType", "application/json");
        this.send(destination, body);
    }

    @Override
    public void render(String templateName) throws ServiceScriptException {
        throw new ServiceScriptException("Invalid send() call, JMS json() must have 2 parameters, send(queueName, content)");
    }

    @Override
    public void render(String templateName, Object templateLocals) throws ServiceScriptException {
        render(templateName);
    }

    public void render(String destination, String templateName) throws ServiceScriptException {
        getActiveMessage().setResponseDestination(destination);
        super.render(templateName);
        completeActiveMessage();
    }

    public void render(String destination, String templateName, Object templateLocals) throws ServiceScriptException {
        getActiveMessage().setResponseDestination(destination);
        super.render(templateName, templateLocals);
        completeActiveMessage();
    }

    // sets the content-type to the mime lookup of type
    public void type(String type) {
        String contentType;
        if (type.contains("/")) {
            contentType = type;
        } else if (!type.contains(".")) {
            contentType = mimeTypes.getContentType("." + type);
        } else {
            contentType = mimeTypes.getContentType(type);
        }
        // set Content-Type header
        header("contenType", contentType);
    }

    @Override
    public RuntimeResponse _getRuntimeResponse(EngineRequest req, EngineResponseMessage message, String body) throws Exception {
        return new JMSRuntimeResponse(body, message.getHeaders(), message.getResponseDestination());
    }

}