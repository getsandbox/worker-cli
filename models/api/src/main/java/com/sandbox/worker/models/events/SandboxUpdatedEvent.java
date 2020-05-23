package com.sandbox.worker.models.events;

import com.sandbox.worker.models.enums.RuntimeVersion;

import java.util.Map;

public class SandboxUpdatedEvent extends SandboxChangeEvent {

    private static final String type = "sandbox_updated_event";

    private String sandboxName;
    private RuntimeVersion oldRuntimeVersion;
    private RuntimeVersion newRuntimeVersion;
    private Map<String, String> updatedProperties;
    private boolean runtimeMigrated;

    public SandboxUpdatedEvent() {
    }

    public SandboxUpdatedEvent(String sandboxId, String sandboxName, RuntimeVersion oldRuntimeVersion, RuntimeVersion newRuntimeVersion,
                               ChangeSource changeSource, Map<String, String> updatedProperties, boolean runtimeMigrated) {
        super(sandboxId, changeSource);
        this.sandboxName = sandboxName;
        this.oldRuntimeVersion = oldRuntimeVersion;
        this.newRuntimeVersion = newRuntimeVersion;
        this.updatedProperties = updatedProperties;
        this.runtimeMigrated = runtimeMigrated;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getSandboxName() {
        return sandboxName;
    }

    public RuntimeVersion getOldRuntimeVersion() {
        return oldRuntimeVersion;
    }

    public RuntimeVersion getNewRuntimeVersion() {
        return newRuntimeVersion;
    }

    public Map<String, String> getUpdatedProperties() {
        return updatedProperties;
    }

    public void setUpdatedProperties(Map<String, String> updatedProperties) {
        this.updatedProperties = updatedProperties;
    }

    public boolean isRuntimeMigrated() {
        return runtimeMigrated;
    }
}
