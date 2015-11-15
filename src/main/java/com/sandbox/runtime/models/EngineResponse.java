package com.sandbox.runtime.models;

import com.sandbox.common.models.RuntimeResponse;
import com.sandbox.common.models.ServiceScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by drew on 30/07/2014.
 */
public abstract class EngineResponse {

    List<EngineResponseMessage> messages = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(EngineResponse.class);

    protected static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    public EngineResponse() {
        //start response with 1 message, HTTP by default will just have one.
        completeActiveMessage();
    }

    protected void completeActiveMessage(){
        messages.add(new EngineResponseMessage());
    }

    public void render(String templateName, Object templateLocals) throws ServiceScriptException {
        EngineResponseMessage message = getActiveMessage();
        message.setTemplateName(templateName);
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
        message.setTemplateLocals(locals);
        message.setRendered(true);
    }

    public void render(String templateName) throws ServiceScriptException {
        EngineResponseMessage message = getActiveMessage();
        message.setTemplateName(templateName);
        message.setRendered(true);
    }

    public void header(String header, String value) {
        getActiveMessage().getHeaders().put(header, value);
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
        return getActiveMessage().getHeaders().get(header);
    }

    // utilities for service
    public boolean wasRendered() { return getActiveMessage().isRendered(); }

    public void setRendered(boolean rendered) {
        getActiveMessage().setRendered(rendered);
    }

    public HashMap<String, String> getHeaders() {
        return getActiveMessage().getHeaders();
    }

    public Object getBody() { return getActiveMessage().getBody(); }

    public void setBody(Object body) {
        getActiveMessage().setBody(body);
    }

    public String getTemplateName() {
        return getActiveMessage().getTemplateName();
    }

    public void setTemplateName(String templateName) {
        getActiveMessage().setTemplateName(templateName);
    }

    public Map getTemplateLocals() {
        return getActiveMessage().getTemplateLocals();
    }

    public void setTemplateLocals(Map templateLocals) {
        getActiveMessage().setTemplateLocals(templateLocals);
    }

    public abstract RuntimeResponse _getRuntimeResponse(EngineRequest req, EngineResponseMessage message, String body) throws Exception;

    protected EngineResponseMessage getActiveMessage(){
        return messages.get(messages.size()-1);
    }

    public List<EngineResponseMessage> getMessages() {
        //if we have more than 1 message, trim off the last one as it will be an empty obj because of completeActiveMessage()
        return messages.size() > 1 ? messages.subList(0, messages.size()-1) : messages;
    }
}