package com.sandbox.worker.core.services;


import com.sandbox.worker.models.RouteConfig;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.SandboxMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryMetadataService implements MetadataService<Object> {

    private SandboxMetadata metadata = null;

    public InMemoryMetadataService() {
    }

    public InMemoryMetadataService(RuntimeVersion runtimeVersion) {
        this(runtimeVersion, new HashMap<>());
    }

    public InMemoryMetadataService(RuntimeVersion runtimeVersion, Map<String, String> config) {
        this(new SandboxMetadata() {
            @Override
            public Map<String, String> getConfig() {
                config.put("sandbox_runtime_version", runtimeVersion.name());
                return config;
            }

            @Override
            public Map<String, RouteConfig> getRouteConfig() {
                return Collections.emptyMap();
            }

            @Override
            public RuntimeVersion getRuntimeVersion() {
                return runtimeVersion;
            }
        });
    }

    public InMemoryMetadataService(SandboxMetadata metadata) {
        this();
        this.metadata = metadata;
    }

    @Override
    public SandboxMetadata getMetadata(String sandboxId) throws Exception {
        return metadata;
    }

    @Override
    public Object getSandbox(String sandboxId) throws Exception {
        return metadata;
    }

    @Override
    public Object getSandboxForSandboxName(String sandboxName) throws Exception {
        return metadata;
    }

    @Override
    public void setSandbox(String sandboxId, String sandboxName, Object sandbox) throws Exception {
        this.metadata = (SandboxMetadata) sandbox;
    }

    @Override
    public void clear(String sandboxId, String sandboxName) {
        metadata = null;
    }
}
