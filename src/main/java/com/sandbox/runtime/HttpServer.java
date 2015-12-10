package com.sandbox.runtime;

import com.sandbox.runtime.services.CommandLineProcessor;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Created by nickhoughton on 18/10/2014.
 */
@Component
@Lazy
public class HttpServer {

    private Server server;

    @Autowired
    HttpRequestHandler handler;

    @Autowired
    Environment environment;

    @Autowired
    CommandLineProcessor commandLine;

    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public void start() {
        int port = commandLine.getHttpPort();
        Path basePath = commandLine.getBasePath();

        server = new Server(port);
        server.setHandler(handler);

        try {
            server.start();

            logger.info("Sandbox ready  --  Running on port: {} with path: '{}'", port, basePath.toAbsolutePath().toRealPath().toString());

            server.join();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting jetty server");
        }

    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        if (server == null) return false;
        return server.isRunning();
    }

}
