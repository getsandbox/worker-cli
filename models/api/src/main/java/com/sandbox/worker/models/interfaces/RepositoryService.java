package com.sandbox.worker.models.interfaces;

public interface RepositoryService {

    String getRepositoryFile(String fullSandboxId, String filename);

    boolean hasRepositoryFile(String fullSandboxId, String filename);
}
