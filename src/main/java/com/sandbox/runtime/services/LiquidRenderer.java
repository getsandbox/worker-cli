package com.sandbox.runtime.services;


import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.jliquid.liqp.Template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickhoughton on 4/08/2014.
 */
public class LiquidRenderer {

    public String render (String templateData, Map<String, Object> parameters) {
        //compile template out of raw string, this could be cached for multiple calls.
        Template compiledTemplate = Template.parse(templateData);

        //render vars into template
        String rendered = compiledTemplate.render(parameters);
        return processResponse(rendered);
    }

    public String render(String templateData) {
        Template compiledTemplate = Template.parse(templateData);

        //render vars into template
        String rendered = compiledTemplate.render();
        return processResponse(rendered);
    }

    public String processResponse(String rendered){
        //remove empty lines, liqp creates empty lines on {% if %} conditions
        return rendered.replaceAll("(?m)^\\s*$[\n\r]{1,}", "");
    }

    public Map prepareValues(Map<String, Object> values){
        return processRenderMap(values);
    }

    //helper functions
    private Map processRenderMap(Map<String, Object> locals) {
        if (locals == null) return null;

        for (String key : locals.keySet()) {
            Object value = locals.get(key);
            locals.put(key, processRenderValue(value));
        }

        return locals;
    }

    private List processRenderCollection(Collection<Object> values) {
        if (values == null) return null;

        List<Object> convertedValues = new ArrayList<>();
        for (Object value : values) {
            convertedValues.add(processRenderValue(value));
        }

        return convertedValues;
    }

    private Object processRenderValue(Object value) {
        if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror objectValue = (ScriptObjectMirror) value;

            if (objectValue.isArray()) {
                return processRenderCollection(objectValue.values());
            } else {
                return processRenderMap(objectValue);
            }

        } else if (value instanceof ScriptObject) {
            ScriptObject objectValue = (ScriptObject) value;

            if (objectValue.isArray()) {
                return processRenderCollection(objectValue.values());

            } else if (objectValue.getMap() != null) {
                Map<String, Object> objectMap = new HashMap<>();
                for (Object key : objectValue.keySet()) objectMap.put((String) key, objectValue.get(key));
                return processRenderMap(objectMap);

            }
        } else if (value instanceof Collection) {
            return processRenderCollection((Collection<Object>) value);

        } else if (value instanceof Map) {
            return processRenderMap((Map<String, Object>) value);

        }

        return value;

    }

}
