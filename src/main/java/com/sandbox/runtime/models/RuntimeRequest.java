package com.sandbox.runtime.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by nickhoughton on 20/10/2014.
 */
public class RuntimeRequest {
    String sandboxId;
    String sandboxName;
    String fullSandboxName;
    String fullSandboxId;
    Map<String, String> headers;
    String body;
    @JsonProperty(value = "content_type")
    String contentType;
    String ip;
    @JsonProperty(value = "received_timestamp")
    long receivedTimestamp = System.currentTimeMillis();

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getSandboxName() {
        return sandboxName;
    }

    public void setSandboxName(String sandboxName) {
        this.sandboxName = sandboxName;
    }

    public String getFullSandboxName() {
        return fullSandboxName;
    }

    public void setFullSandboxName(String fullSandboxName) {
        this.fullSandboxName = fullSandboxName;
    }

    public String getFullSandboxId() {
        return fullSandboxId;
    }

    public void setFullSandboxId(String fullSandboxId) {
        this.fullSandboxId = fullSandboxId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getReceivedTimestamp() {
        return receivedTimestamp;
    }
}
