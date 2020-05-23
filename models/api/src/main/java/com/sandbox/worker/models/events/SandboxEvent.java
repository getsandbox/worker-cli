package com.sandbox.worker.models.events;

public abstract class SandboxEvent extends Event {

    private String sandboxId;

    public SandboxEvent() {
    }

    public SandboxEvent(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }
}
