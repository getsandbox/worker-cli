package com.sandbox.runtime.models;

/**
 * Created by nickhoughton on 18/10/2014.
 */
public interface Cache {
    String getRepositoryFile(String fullSandboxId, String filename);

    void updateRemoteRepositoryFiles(String fullSandboxId);

    boolean hasRepositoryFile(String fullSandboxId, String filename);

    String getSandboxState(String sandboxId);

    void setSandboxState(String sandboxId, String state);

    public void setRoutingTableForSandboxId(String sandboxId, RoutingTable routingTable);

    public RoutingTable getRoutingTableForSandboxId(String sandboxId);
}
