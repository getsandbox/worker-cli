package com.sandbox.worker.models.events;

public class WorkerInactiveEvent extends WorkerEvent {

    private static final String type = "worker_inactive_event";

    public WorkerInactiveEvent() {
    }

    public WorkerInactiveEvent(String workerId) {
        super(workerId);
    }

    @Override
    public String getType() {
        return type;
    }

}
