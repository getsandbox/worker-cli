package com.sandbox.runtime.models;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by drew on 30/07/2014.
 */
public class HTTPResponse {

    private Object body;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private List<String[]> cookies = new ArrayList<String[]>();
    private Integer status = null;

    private boolean rendered;
    private String templateName;
    private Map templateLocals;

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
    private static final Logger logger = LoggerFactory.getLogger(HTTPResponse.class);

    // Content-Type defaulted to 'application/json'
    public void send(Object body) {
        if (body instanceof ScriptObject || body instanceof Map || body instanceof Collection) {
            // if Content-Type not already set then do it.
            if (!headers.containsKey("Content-Type"))
                headers.put("Content-Type", "application/json");
        } else {
            // treat everything else as plain text
            if (!headers.containsKey("Content-Type"))
                headers.put("Content-Type", "text/plain");
        }

        this.body = body;

        // set route matched
        rendered = false;
    }

    public void send(int status, Object body) {
        this.status = status;
        this.send(body);
    }

    public void send(NativeArray body) {
        // if Content-Type not already set then do it.
        if (!headers.containsKey("Content-Type"))
            headers.put("Content-Type", "application/json");

        this.body = body;

        rendered = false;
    }

    public void send(int status, NativeArray body) {
        this.status = status;
        this.send(body);
    }

    /**
     * Always set Content-Type header to application/json. Convert the JS object to string
     * @param body
     */
    public void json(int status, Object body) {
        this.status = status;
        this.json(body);
    }

    public void json(Object body) {
        headers.put("Content-Type", "application/json");
        this.send(body);
    }

    public void json(ScriptObject body) {
        headers.put("Content-Type", "application/json");
        this.send(body);
    }

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

    // if user passes a non-int param Nashorn converts it to 0.
    // could use ScriptMirror, inspect type and throw if not an int?
    public HTTPResponse status(int _status) {
        status = _status;
        return this;
    }

    public void statusCode(int _status) { status(_status); }

    public Integer getStatus() { return status; }

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

    //TODO this is a super simple version, make a better one that does other props of cookies.
    public void cookie(String name, String value) {
        if(name != null && value != null) cookies.add(new String[]{name,value});
    }

    public void clearCookie() { }

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
        header("Content-Type", contentType);
    }

    public void links() { }


    // utilities for service
    public boolean wasRendered() { return rendered; }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Object getBody() { return body; }

    public String getTemplateName() {
        return templateName;
    }

    public Map getTemplateLocals() {
        return templateLocals;
    }

    public List<String[]> getCookies() {
        return cookies;
    }
}