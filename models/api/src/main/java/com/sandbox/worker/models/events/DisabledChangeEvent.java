package com.sandbox.worker.models.events;

public class DisabledChangeEvent extends Event {
    @Override
    public String getType() {
        return "disabled_change_event";
    }
}
