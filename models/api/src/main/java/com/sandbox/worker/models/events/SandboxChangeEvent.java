package com.sandbox.worker.models.events;

public abstract class SandboxChangeEvent extends SandboxEvent {

    private ChangeSource changeSource = ChangeSource.USER;

    public SandboxChangeEvent() {
    }

    public SandboxChangeEvent(String sandboxId, ChangeSource changeSource) {
        super(sandboxId);
        this.changeSource = changeSource;
    }

    public ChangeSource getChangeSource() {
        return changeSource;
    }

    public void setChangeSource(ChangeSource changeSource) {
        this.changeSource = changeSource;
    }

    public enum ChangeSource {
        USER,
        SYNC_SERVICE,
        SYSTEM
    }
}
