package com.sandbox.worker.models.events;

public class WorkerContextInactiveEvent extends WorkerEvent {

    private static final String type = "worker_context_inactive_event";
    private String sandboxId;

    public WorkerContextInactiveEvent() {
    }

    public WorkerContextInactiveEvent(String workerId, String sandboxId) {
        super(workerId);
        this.sandboxId = sandboxId;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getSandboxId() {
        return sandboxId;
    }

}
