package com.sandbox.worker.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "transport",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpRuntimeResponse.class, name = "HTTP"),
})
public abstract class RuntimeResponse {
    @ApiModelProperty(value = "Which transport the request was for, 'HTTP'.")
    protected String transport;
    @ApiModelProperty(value = "The body of the given response.")
    protected String body;
    @ApiModelProperty(value = "Transport headers for the given response.")
    protected Map<String, String> headers;
    @Deprecated @ApiModelProperty(value = "Error if there is a problem during Sandbox execution.")
    protected Error error;
    @ApiModelProperty(value = "The epoch time in milliseconds when the response was sent.")
    private long respondedTimestamp = System.currentTimeMillis();
    @ApiModelProperty(value = "Duration in milliseconds of the processing time in Sandbox.")
    private long durationMillis;
    @ApiModelProperty(value = "Duration in milliseconds of the response delay.")
    private int responseDelay = 0;

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

    public int getResponseDelay() {
        return responseDelay;
    }

    public void setResponseDelay(int responseDelay) {
        this.responseDelay = responseDelay;
    }
}
