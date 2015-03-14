package com.sandbox.runtime.models;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by drew on 30/07/2014.
 */
public abstract class EngineResponse {

    private Object body;
    private HashMap<String, String> headers = new HashMap<String, String>();

    private boolean rendered;
    private String templateName;
    private Map templateLocals;

    private static final Logger logger = LoggerFactory.getLogger(EngineResponse.class);

    public void render(String templateName, Object templateLocals) {
        this.templateName = templateName;
        Map locals = null;

        //be defensive against crap being passed in, only maps are supported. Could be 'Undefined' or any junk.
        if(templateLocals instanceof Map) {
            locals = (Map) templateLocals;
        }else if(templateLocals instanceof ScriptObject){
            ScriptObject localProperties = (ScriptObject) templateLocals;
            Map<String, Object> convertedMap = new LinkedHashMap<String, Object>();
            localProperties.propertyIterator().forEachRemaining((k) -> {
                convertedMap.put(k, localProperties.get(k));
            });
            locals = convertedMap;
        }else{
            logger.error("Invalid object passed to render(), return new map, {}",templateLocals.getClass());
            locals = new HashMap<>();
        }
        this.templateLocals = locals;
        rendered = true;
    }

    public void render(String templateName) {
        this.templateName = templateName;
        rendered = true;
    }

    public void header(String header, String value) {
        headers.put(header, value);
    }

    public void set(String header, String value) {
        header(header, value);
    }

    public void set(ScriptObjectMirror obj) {
        header(obj); }

    // concat the array to a string
    // forces conversions of values to string types
    public void header(String header, String[] values) {
        String value = Arrays.asList(values).stream().collect(Collectors.joining(","));
        this.header(header, value);
    }

    public void header(ScriptObjectMirror obj) {
        for (String key: obj.getOwnKeys(true)) {
            this.header(key, obj.get(key).toString());
        }
    }

    public String get(String header) {
        return headers.get(header);
    }

    // utilities for service
    public boolean wasRendered() { return rendered; }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Object getBody() { return body; }

    public void setBody(Object body) {
        this.body = body;
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

    public abstract RuntimeResponse _getRuntimeResponse(EngineRequest req, String body) throws Exception;
}