package com.sandbox.worker.models.events;

import com.sandbox.worker.models.ActivityMessage;

import java.util.List;

public class SandboxRequestEvent extends SandboxEvent {

    private static final String type = "worker_request_event";

    private List<ActivityMessage> activityMessages;

    public SandboxRequestEvent() {
    }

    public SandboxRequestEvent(String sandboxId, List<ActivityMessage> activityMessages) {
        super(sandboxId);
        this.activityMessages = activityMessages;
    }

    @Override
    public String getType() {
        return type;
    }

    public List<ActivityMessage> getActivityMessages() {
        return activityMessages;
    }

    public void setActivityMessages(List<ActivityMessage> activityMessages) {
        this.activityMessages = activityMessages;
    }
}
