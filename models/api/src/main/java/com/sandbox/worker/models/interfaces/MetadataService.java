package com.sandbox.worker.models.interfaces;

public interface MetadataService<T> {

    SandboxMetadata getMetadata(String sandboxId) throws Exception;

    T getSandbox(String sandboxId) throws Exception;

    T getSandboxForSandboxName(String sandboxName) throws Exception;

    void setSandbox(String sandboxId, String sandboxName, T sandbox) throws Exception;

    void clear(String sandboxId, String sandboxName);
}
