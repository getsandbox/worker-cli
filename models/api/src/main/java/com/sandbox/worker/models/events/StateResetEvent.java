package com.sandbox.worker.models.events;

public class StateResetEvent extends SandboxChangeEvent {

    private static final String type = "state_reset_event";

    public StateResetEvent() {
    }

    public StateResetEvent(String sandboxId, ChangeSource changeSource) {
        super(sandboxId, changeSource);
    }

    @Override
    public String getType() {
        return type;
    }

}
