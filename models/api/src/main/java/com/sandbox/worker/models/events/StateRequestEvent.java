package com.sandbox.worker.models.events;

public class StateRequestEvent extends SandboxChangeEvent {

    private static final String type = "state_request_event";

    public StateRequestEvent() {
    }

    public StateRequestEvent(String sandboxId, ChangeSource changeSource) {
        super(sandboxId, changeSource);
    }

    @Override
    public String getType() {
        return type;
    }

}
