package com.sandbox.worker.models.interfaces;

public interface StateService {

    String getSandboxState(String sandboxId);

    void setSandboxState(String sandboxId, String state);

}
