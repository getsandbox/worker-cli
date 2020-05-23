package com.sandbox.worker.core.js.models;

import com.sandbox.worker.core.graal.ValueMapWrapper;

import javax.activation.MimetypesFileTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HostAccess.Implementable
public class WorkerHttpResponse {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerHttpResponse.class);
    private static final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    private Integer statusCode = 200;
    private String statusText = null;
    private HashMap<String, String> headers = new HashMap<>();
    private List<String[]> cookies = new ArrayList<String[]>();
    private Object body;

    private boolean rendered;
    private String templateName;
    private Map templateLocals;

    private int responseDelay = 0;
    private boolean responseConfigured = false;

    @HostAccess.Export
    public WorkerHttpResponse status(int statusCode) {
        this.responseConfigured = true;
        this.statusCode = statusCode;
        return this;
    }

    @HostAccess.Export
    public WorkerHttpResponse status(int statusCode, String statusText) {
        status(statusCode);
        this.statusText = statusText;
        return this;
    }


    @HostAccess.Export
    public void send(int status) {
        status(status);
        send("");
    }
    
    // Content-Type defaulted to 'application/json'
    @HostAccess.Export
    public void send(Object body) {
        if(body != null){
            this.responseConfigured = true;
        }

        if (body instanceof Map || body instanceof Collection) {
            // if Content-Type not already set then do it.
            if (!getHeaders().containsKey("Content-Type"))
                getHeaders().put("Content-Type", "application/json");
        } else {
            // treat everything else as plain text
            if (!getHeaders().containsKey("Content-Type"))
                getHeaders().put("Content-Type", "text/plain");
        }

        setBody(body);

        // set route matched
        setRendered(false);
    }

    @HostAccess.Export
    public void send(int status, Object body) {
        status(status);
        send(body);
    }

    @HostAccess.Export
    public void json(Object body) {
        header("Content-Type", "application/json");
        send(body);
    }

    /**
     * Always set Content-Type header to application/json. Convert the JS object to string
     *
     * @param body
     */
    @HostAccess.Export
    public void json(int status, Object body) {
        status(status);
        json(body);
    }

    @HostAccess.Export
    public void render(String templateName, Object templateLocals) {
        this.responseConfigured = true;
        setTemplateName(templateName);
        Map locals = null;

        //be defensive against crap being passed in, only maps are supported. Could be 'Undefined' or any junk.
        if (templateLocals instanceof Map) {
            locals = (Map) templateLocals;
        } else if(templateLocals instanceof Value){
            locals = ValueMapWrapper.fromValue((Value) templateLocals);
        } else {
            LOG.error("Invalid object passed to render(), return new map, {}", templateLocals.getClass());
            locals = new HashMap<>();
        }
        setTemplateLocals(locals);
        setRendered(true);
    }

    @HostAccess.Export
    public void render(String templateName, Value templateLocals) {
        render(templateName, (Object) templateLocals);
    }


    @HostAccess.Export
    public void render(String templateName) {
        this.responseConfigured = true;
        setTemplateName(templateName);
        setRendered(true);
    }

    @HostAccess.Export
    public void header(String header, String value) {
        this.responseConfigured = true;
        getHeaders().put(header, value);
    }

    @HostAccess.Export
    public void set(String header, String value) {
        header(header, value);
    }

    // concat the array to a string
    // forces conversions of values to string types
    @HostAccess.Export
    public void header(String header, String[] values) {
        String value = Arrays.asList(values).stream().collect(Collectors.joining(","));
        header(header, value);
    }

    @HostAccess.Export
    public String get(String header) {
        return getHeaders().get(header);
    }

    @HostAccess.Export
    public void delay(int delay) {
        setResponseDelay(delay);
    }

    @HostAccess.Export
    public boolean isResponseConfigured() {
        return responseConfigured;
    }

    @HostAccess.Export
    public boolean wasRendered() {
        return rendered;
    }

    @HostAccess.Export
    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    @HostAccess.Export
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    @HostAccess.Export
    public Object getBody() {
        return body;
    }

    @HostAccess.Export
    public void setBody(Object body) {
        this.body = body;
    }

    @HostAccess.Export
    public String getTemplateName() {
        return templateName;
    }

    @HostAccess.Export
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @HostAccess.Export
    public Map getTemplateLocals() {
        return templateLocals;
    }

    @HostAccess.Export
    public void setTemplateLocals(Map templateLocals) {
        this.templateLocals = templateLocals;
    }

    @HostAccess.Export
    public int getResponseDelay() {
        return responseDelay;
    }

    @HostAccess.Export
    public void setResponseDelay(int responseDelay) {
        this.responseDelay = responseDelay;
    }

    @HostAccess.Export
    public void statusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @HostAccess.Export
    public void statusText(String statusText) {
        this.statusText = statusText;
    }

    @HostAccess.Export
    public Integer getStatusCode() {
        return statusCode;
    }

    @HostAccess.Export
    public String getStatusText() {
        return statusText;
    }

    //TODO this is a super simple version, make a better one that does other props of cookies.
    @HostAccess.Export
    public void cookie(String name, String value) {
        if (name != null && value != null) cookies.add(new String[]{name, value});
    }

    @HostAccess.Export
    public void clearCookie() {
    }

    // sets the content-type to the mime lookup of type
    @HostAccess.Export
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

    @HostAccess.Export
    public void links() {
    }

    @HostAccess.Export
    public List<String[]> getCookies() {
        return cookies;
    }

}