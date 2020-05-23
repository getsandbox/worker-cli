package com.sandbox.worker.cli.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.worker.cli.config.Config;
import com.sandbox.worker.models.RouteConfig;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.SandboxMetadata;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileMetadataService implements MetadataService<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileMetadataService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private Config appConfig;
    private File configPath;

    public LocalFileMetadataService(File configPath) {
        this.configPath = configPath;
    }

    public LocalFileMetadataService(Config config) {
        this.appConfig = config;
        this.configPath = config.getConfigPath();
    }

    @Override
    public SandboxMetadata getMetadata(String sandboxId) throws Exception {
        return new SandboxMetadata() {
            @Override
            public Map<String, String> getConfig() throws Exception {
                Map<String, String> config;
                if(configPath != null && configPath.exists()){
                    config = mapper.readValue(configPath, Map.class);
                } else {
                    config = new HashMap();
                }
                config.put("sandbox_runtime_version", appConfig.getVersion().name());
                return config;
            }

            @Override
            public Map<String, RouteConfig> getRouteConfig() {
                return Collections.emptyMap();
            }

            @Override
            public RuntimeVersion getRuntimeVersion() {
                return appConfig.getVersion();
            }
        };
    }

    @Override
    public ObjectNode getSandbox(String sandboxId) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ObjectNode getSandboxForSandboxName(String sandboxName) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setSandbox(String sandboxId, String sandboxName, Object sandbox) throws Exception {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clear(String sandboxId, String sandboxName) {
        //noop
    }
}
