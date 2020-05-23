package com.sandbox.worker.models;

public class SandboxIdentifier {

    String sandboxId;
    String fullSandboxId;

    @Deprecated
    //for jackson only, use constructor.
    public SandboxIdentifier() {
    }

    public SandboxIdentifier(String sandboxId) {
        this.sandboxId = sandboxId;
        this.fullSandboxId = sandboxId;
    }

    public SandboxIdentifier(String sandboxId, String fullSandboxId) {
        this.sandboxId = sandboxId;
        this.fullSandboxId = fullSandboxId;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getFullSandboxId() {
        return fullSandboxId;
    }

    public void setFullSandboxId(String fullSandboxId) {
        this.fullSandboxId = fullSandboxId;
    }

    @Override
    public int hashCode() {
        int result = sandboxId.hashCode();
        result = 31 * result + fullSandboxId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof String) return o.equals(getSandboxId());

        SandboxIdentifier that = (SandboxIdentifier) o;

        if (!sandboxId.equals(that.sandboxId)) return false;
        return fullSandboxId.equals(that.fullSandboxId);

    }

    @Override
    public String toString() {
        return sandboxId + " (" + fullSandboxId + ")";
    }
}
