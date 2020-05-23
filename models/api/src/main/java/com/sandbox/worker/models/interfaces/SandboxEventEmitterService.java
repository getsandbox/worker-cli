package com.sandbox.worker.models.interfaces;

import com.sandbox.worker.models.ActivityMessage;
import com.sandbox.worker.models.events.Event;
import com.sandbox.worker.models.events.SandboxRequestEvent;

import java.util.Arrays;
import java.util.List;

public interface SandboxEventEmitterService extends EventEmitterService<Event> {

    default void logActivityMessages(String sandboxId, List<ActivityMessage> messages) {
        SandboxRequestEvent event = new SandboxRequestEvent(sandboxId, messages);
        emitEvent(event);
    }

    default void logActivityMessage(ActivityMessage message) {
        logActivityMessages(message.getSandboxId(), Arrays.asList(message));
    }

    default void emitActivity(ActivityMessage activity) {
        logActivityMessage(activity);
    }
}
