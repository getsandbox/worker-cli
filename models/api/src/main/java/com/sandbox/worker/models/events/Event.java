package com.sandbox.worker.models.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WorkerInactiveEvent.class, name = "worker_inactive_event"),
        @JsonSubTypes.Type(value = WorkerContextInactiveEvent.class, name = "worker_context_inactive_event"),
        @JsonSubTypes.Type(value = SandboxRequestEvent.class, name = "worker_request_event"),
        @JsonSubTypes.Type(value = SandboxUpdatedEvent.class, name = "sandbox_updated_event"),
        @JsonSubTypes.Type(value = SandboxDeletedEvent.class, name = "sandbox_deleted_event"),
        @JsonSubTypes.Type(value = FileChangeEvent.class, name = "file_change_event"),
        @JsonSubTypes.Type(value = StateResetEvent.class, name = "state_reset_event"),
        @JsonSubTypes.Type(value = StateRequestEvent.class, name = "state_request_event"),
        @JsonSubTypes.Type(value = DisabledChangeEvent.class, name = "disabled_change_event"),
})
public abstract class Event {

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public abstract String getType();

}
