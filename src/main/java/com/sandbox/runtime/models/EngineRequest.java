package com.sandbox.runtime.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.runtime.js.converters.NashornConverter;
import com.sandbox.runtime.js.models.JsonNode;
import com.sandbox.runtime.utils.URISupport;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public abstract class EngineRequest {

    final Map<String, String> headers;
    final Map<String, String> properties;
    final Object body;
    final String contentType;
    final String ip;
    final XMLDoc xmlDoc;

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    public EngineRequest(ScriptEngine scriptEngine, Map<String, String> headers,
                         Map<String, String> properties, Object body, String contentType, String ip) throws ServiceScriptException {

        // set default values
        this.headers = headers != null ? headers : new HashMap<String, String>();
        this.properties = properties != null ? properties : new HashMap<String, String>();
        this.contentType = contentType != null ? contentType : "";
        this.ip = ip != null ? ip : "";

        Object _body = null;
        XMLDoc _xmlDoc = null;

        // if the body is non-zero length then parse it
        if (body != null && !body.toString().isEmpty()) {
            try {
                if ("json".equalsIgnoreCase(this.contentType)) {
                    _body = new JsonNode((String) body).getJsonObject();
                    //convert java arrays/maps to JS ones
                    _body = NashornConverter.instance().convert(scriptEngine, _body);

                } else if ("xml".equalsIgnoreCase(this.contentType)) {
                    _xmlDoc = new XMLDoc(body);

                } else if ("urlencoded".equalsIgnoreCase(this.contentType)){
                    _body = decodeBody((String) body);

                } else {
                    _body = body;
                }
            } catch(Exception e) {
                throw new ServiceScriptException("Can't parse body of type " + this.contentType);
            }
        }

        this.body = _body;
        this.xmlDoc = _xmlDoc;
    }

    public String get(String headerName){
        if(getHeaders() == null) return null;
        return getHeaders().get(headerName);
    }

    public boolean is(String type){
        if(type == null || type.length() == 0) return false;
        if(headers == null || headers.get("Content-Type") == null) return false;

        String contentType;
        if (type.contains("/")) {
            contentType = type;
        } else if (!type.contains(".")) {
            contentType = mimeTypes.getContentType("." + type);
        } else {
            contentType = mimeTypes.getContentType(type);
        }

        return headers.get("Content-Type").toLowerCase().startsWith(contentType.toLowerCase());
    }


    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Object getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getIp() {
        return ip;
    }

    @JsonIgnore
    public XMLDoc getXmlDoc() {
        return xmlDoc;
    }

    @JsonIgnore
    public String getBodyAsString() { return body == null ? "" : body.toString(); }

    private HashMap<String, String> decodeBody(String body) throws Exception{
        HashMap<String, String> queryMap = new HashMap<>();

        try {
            Map<String, Object> params = URISupport.parseQuery(body);
            for (String key : params.keySet()) {
                Object value = params.get(key);
                if(value instanceof String) {
                    value = URLDecoder.decode((String) value, "UTF-8");
                    queryMap.put(key, (String) value);
                }
                if(value instanceof List){
                    for (Object listValue : (List)value){
                        listValue = URLDecoder.decode((String) listValue, "UTF-8");
                        queryMap.put(key, (String) listValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to parse urlencoded body");
        }

        return queryMap;
    }

    private static List<String> accessibleProperties = new ArrayList();
    public List<String> _getAccessibleProperties() {
        if(!accessibleProperties.isEmpty()) return accessibleProperties;

        for (Field field : this.getClass().getDeclaredFields()){
            if(!Modifier.isPrivate(field.getModifiers())) accessibleProperties.add(field.getName());
        }

        for (Method method: this.getClass().getDeclaredMethods()){
            if(Modifier.isPublic(method.getModifiers())) accessibleProperties.add(method.getName());
        }

        return accessibleProperties;
    }
}