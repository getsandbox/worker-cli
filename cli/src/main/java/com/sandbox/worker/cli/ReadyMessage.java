package com.sandbox.worker.cli;

import com.sandbox.worker.cli.config.Config;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.server.event.ServerStartupEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Introspected
@Singleton
public class ReadyMessage implements ApplicationEventListener<ServerStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ReadyMessage.class);

    @Inject
    private Config config;

    @Inject
    private Environment environment;

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
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
