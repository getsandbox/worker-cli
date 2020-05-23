package com.sandbox.worker.models.events;

public abstract class WorkerEvent extends Event {

    private String workerId;

    public WorkerEvent() {
    }

    public WorkerEvent(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
}
