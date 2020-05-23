package com.sandbox.worker.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "transport",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpRuntimeRequest.class, name = "HTTP"),
})
public abstract class RuntimeRequest {
    @ApiModelProperty(value = "Which transport the request was for, 'HTTP'.")
    String transport;
    @ApiModelProperty(value = "The ID of the Sandbox that received the request.")
    String sandboxId;
    @ApiModelProperty(value = "The name of the Sandbox that received the request.")
    String sandboxName;
    @ApiModelProperty(value = "The parent name of the Sandbox that received the request.")
    String fullSandboxName;
    @ApiModelProperty(value = "The parent ID of the Sandbox that received the request.")
    String fullSandboxId;
    @ApiModelProperty(value = "Transport headers for the given request.")
    Map<String, String> headers;
    Map<String, String> properties = new HashMap<>();
    @ApiModelProperty(value = "The body of the given request.")
    String body;
    @ApiModelProperty(value = "The content type of the body, for example 'application/json'.")
    String contentType;
    @ApiModelProperty(value = "The requestor IP address.")
    String ip;
    @ApiModelProperty(value = "The epoch time in milliseconds when the request was received.")
    long receivedTimestamp = System.currentTimeMillis();

    public abstract String getTransport();

    public void setTransport(String transport) {
        //noop, only here to keep jackson happy.
    }

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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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
