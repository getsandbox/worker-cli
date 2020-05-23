package com.sandbox.worker.models;

import com.sandbox.worker.models.enums.RuntimeTransportType;

import java.util.List;
import java.util.Map;

public class HttpRuntimeResponse extends RuntimeResponse {

    private int statusCode;

    private String statusText;

    private List<String[]> cookies;

    public HttpRuntimeResponse() {
    }

    public HttpRuntimeResponse(String body, int statusCode, String statusText, Map<String, String> headers, List<String[]> cookies) {
        this.body = body;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.cookies = cookies;
        this.error = null;
    }

    public HttpRuntimeResponse(Error error) {
        this.error = error;
        this.statusCode = 500;

    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public List<String[]> getCookies() {
        return cookies;
    }

    @Override
    public String getTransport() {
        return RuntimeTransportType.HTTP.toString();
    }
}
