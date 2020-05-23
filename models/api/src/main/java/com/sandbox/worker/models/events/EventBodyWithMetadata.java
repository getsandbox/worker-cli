package com.sandbox.worker.models.events;

public class EventBodyWithMetadata {
    private String body;
    private String sandboxId;

    public EventBodyWithMetadata(String body, String sandboxId) {
        this.body = body;
        this.sandboxId = sandboxId;
    }

    public String getBody() {
        return body;
    }

    public String getSandboxId() {
        return sandboxId;
    }
}
