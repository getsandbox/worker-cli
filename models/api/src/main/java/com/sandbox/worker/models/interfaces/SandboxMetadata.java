package com.sandbox.worker.models.interfaces;

import com.sandbox.worker.models.RouteConfig;
import com.sandbox.worker.models.enums.RuntimeVersion;

import java.util.Map;

public interface SandboxMetadata {

    Map<String, String> getConfig() throws Exception;

    Map<String, RouteConfig> getRouteConfig();

    RuntimeVersion getRuntimeVersion();
}
