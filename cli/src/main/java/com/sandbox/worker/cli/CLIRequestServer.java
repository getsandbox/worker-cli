package com.sandbox.worker.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.cli.config.Config;
import com.sandbox.worker.core.server.RequestHandler;
import com.sandbox.worker.core.server.RequestServer;
import io.micronaut.context.env.Environment;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CLIRequestServer extends RequestServer {
    private static final Logger LOG = LoggerFactory.getLogger(CLIRequestServer.class);

    private Config config;

    @Inject
    private Environment environment;

    public CLIRequestServer(Config config, RequestHandler requestFilter, ObjectMapper mapper) {
        super(config.getRequestListenerPort(), requestFilter, mapper);
        this.config = config;
    }

    @Override
    public void onStart() {
        try {
            LOG.info("Sandbox ready (build: v{} runtime: {}) --  Running on port: {}, metadata on port: {}, reading from path: '{}', took: {}ms",
                    environment.getProperty("SANDBOX_VERSION", String.class, "?"),
                    config.getVersion(),
                    config.getRequestListenerPort(),
                    config.getActivityListenerPort() < 0 ? "disabled" : config.getActivityListenerPort(),
                    config.getBasePath().toPath().toRealPath().toString(),
                    ManagementFactory.getRuntimeMXBean().getUptime()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
