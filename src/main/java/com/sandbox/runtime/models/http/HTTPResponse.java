package com.sandbox.runtime.models.http;

import com.sandbox.common.models.RuntimeResponse;
import com.sandbox.common.models.http.HttpRuntimeResponse;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.EngineResponseMessage;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class HTTPResponse extends EngineResponse {

    private List<String[]> cookies = new ArrayList<String[]>();
    private Integer statusCode = null;
    private String statusText = null;

    public void send(int status) {
        this.statusCode = status;
        this.send("");
    }

    // Content-Type defaulted to 'application/json'
    public void send(Object body) {
        if (body instanceof ScriptObject || body instanceof Map || body instanceof Collection) {
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

    public void send(int status, Object body) {
        this.statusCode = status;
        this.send(body);
    }

    public void send(ScriptObject body) {
        // if Content-Type not already set then do it.
        if (!getHeaders().containsKey("Content-Type"))
            getHeaders().put("Content-Type", "application/json");

        setBody(body);

        setRendered(false);
    }

    public void send(int status, ScriptObject body) {
        this.statusCode = status;
        this.send(body);
    }

    public void json(Object body) {
        getHeaders().put("Content-Type", "application/json");
        this.send(body);
    }

    /**
     * Always set Content-Type header to application/json. Convert the JS object to string
     * @param body
     */
    public void json(int status, Object body) {
        this.statusCode = status;
        this.json(body);
    }

    public void json(ScriptObject body) {
        getHeaders().put("Content-Type", "application/json");
        this.send(body);
    }

    public void json(int status, ScriptObject body) {
        this.statusCode = status;
        getHeaders().put("Content-Type", "application/json");
        this.send(body);
    }

    // if user passes a non-int param Nashorn converts it to 0.
    // could use ScriptMirror, inspect type and throw if not an int?
    public HTTPResponse status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HTTPResponse status(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        return this;
    }

    public void statusCode(int statusCode) {
        status(statusCode);
    }

    public void statusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getStatusCode() { return statusCode; }

    public String getStatusText() {
        return statusText;
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

    public List<String[]> getCookies() {
        return cookies;
    }

    public RuntimeResponse _getRuntimeResponse(EngineRequest req, EngineResponseMessage message, String body) throws Exception {
        // check for a status code being set
        // if an exception is thrown above, the Proxy will see the error at its end
        // and replace the status code with 500
        if (getStatusCode() == null) {
            status(200);
        }

        return new HttpRuntimeResponse(body, getStatusCode(), getStatusText(), getHeaders(), getCookies());
    }
}