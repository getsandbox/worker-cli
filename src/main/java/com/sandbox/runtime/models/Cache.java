package com.sandbox.runtime.models;

import java.util.Map;

/**
 * Created by nickhoughton on 18/10/2014.
 */
public interface Cache {
    String getRepositoryFile(String fullSandboxId, String filename);

    boolean hasRepositoryFile(String fullSandboxId, String filename);

    String getSandboxState(String sandboxId);

    void setSandboxState(String sandboxId, String state);

    public void setRoutingTableForSandboxId(String sandboxId, String fullSandboxId, RoutingTable routingTable);

    public RoutingTable getRoutingTableForSandboxId(String sandboxId, String fullSandboxId);

    public Map<String, String> getConfigForSandboxId(String sandboxId);
}
