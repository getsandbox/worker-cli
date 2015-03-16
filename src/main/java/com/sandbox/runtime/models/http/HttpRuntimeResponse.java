package com.sandbox.runtime.models.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sandbox.runtime.models.*;
import com.sandbox.runtime.models.enums.RuntimeTransportType;

import java.util.List;
import java.util.Map;

/**
 * Created by drew on 6/08/2014.
 */
public class HttpRuntimeResponse extends RuntimeResponse {

    @JsonProperty(value = "status")
    private int statusCode;

    private List<String[]> cookies;

    public HttpRuntimeResponse() {
    }

    public HttpRuntimeResponse(String body, int statusCode, Map<String, String> headers, List<String[]> cookies) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
        this.cookies = cookies;
        this.error = null;
    }

    public HttpRuntimeResponse(com.sandbox.runtime.models.Error error) {
        this.error = error;
        this.statusCode = 500;

    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<String[]> getCookies() {
        return cookies;
    }

    @Override
    public String getTransport() {
        return RuntimeTransportType.HTTP.toString();
    }
}
