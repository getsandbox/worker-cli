package com.sandbox.runtime.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by nickhoughton on 20/10/2014.
 */
public abstract class RuntimeResponse {
    protected String transport;
    protected String body;
    protected Map<String, String> headers;
    protected Error error;
    @JsonProperty(value = "responded_timestamp")
    private long respondedTimestamp = System.currentTimeMillis();
    @JsonProperty(value = "duration_ms")
    private long durationMillis;

    public abstract String getTransport();

    public void setTransport(String transport) {
        //noop, only here to keep jackson happy.
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Error getError() {
        return error;
    }

    public boolean isError() {
        return (error != null);
    }

    public Long getRespondedTimestamp() {
        return respondedTimestamp;
    }

    public void setRespondedTimestamp(Long respondedTimestamp) {
        this.respondedTimestamp = respondedTimestamp;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
