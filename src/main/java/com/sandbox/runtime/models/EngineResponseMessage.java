package com.sandbox.runtime.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickhoughton on 21/05/2015.
 */
public class EngineResponseMessage {

    private Object body;
    private HashMap<String, String> headers = new HashMap<String, String>();

    private boolean rendered;
    private String templateName;
    private Map templateLocals;

    //used for JMS, ignored for HTTP
    private String responseDestination;

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public boolean isRendered() {
        return rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map getTemplateLocals() {
        return templateLocals;
    }

    public void setTemplateLocals(Map templateLocals) {
        this.templateLocals = templateLocals;
    }

    public String getResponseDestination() {
        return responseDestination;
    }

    public void setResponseDestination(String responseDestination) {
        this.responseDestination = responseDestination;
    }
}
