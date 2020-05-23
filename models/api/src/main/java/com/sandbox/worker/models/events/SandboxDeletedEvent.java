package com.sandbox.worker.models.events;

public class SandboxDeletedEvent extends SandboxChangeEvent {

    private static final String type = "sandbox_deleted_event";

    public SandboxDeletedEvent() {
    }

    public SandboxDeletedEvent(String sandboxId, ChangeSource changeSource) {
        super(sandboxId, changeSource);
    }

    @Override
    public String getType() {
        return type;
    }

}
