package com.sandbox.runtime.models.http;

import com.sandbox.common.models.http.HttpRuntimeResponse;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.EngineResponseMessage;
import com.sandbox.common.models.RuntimeResponse;
import jdk.nashorn.internal.objects.NativeArray;
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
    private Integer status = null;

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
        this.status = status;
        this.send(body);
    }

    public void send(NativeArray body) {
        // if Content-Type not already set then do it.
        if (!getHeaders().containsKey("Content-Type"))
            getHeaders().put("Content-Type", "application/json");

        setBody(body);

        setRendered(false);
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
        getHeaders().put("Content-Type", "application/json");
        this.send(body);
    }

    // if user passes a non-int param Nashorn converts it to 0.
    // could use ScriptMirror, inspect type and throw if not an int?
    public HTTPResponse status(int _status) {
        status = _status;
        return this;
    }

    public void statusCode(int _status) { status(_status); }

    public Integer getStatus() { return status; }

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
        if (getStatus() == null) {
            status(200);
        }

        return new HttpRuntimeResponse(body, getStatus(), getHeaders(), getCookies());
    }
}