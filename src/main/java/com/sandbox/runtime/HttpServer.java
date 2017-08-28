package com.sandbox.runtime;

import com.sandbox.runtime.models.config.RuntimeConfig;

import java.nio.file.Path;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
    RuntimeConfig config;

    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public void start() {
        int port = config.getHttpPort();
        Path basePath = config.getBasePath();

        //check for override values from jvm args
        String jettyAcceptorStr = System.getProperty("JETTY_ACCEPTOR");
        int jettyAcceptorThreads = Integer.parseInt(jettyAcceptorStr == null ? "1" : jettyAcceptorStr);
        String jettySelectorStr = System.getProperty("JETTY_SELECTOR");
        int jettySelectorThreads = Integer.parseInt(jettySelectorStr == null ? "2" : jettySelectorStr);
        String jettyRequestStr = System.getProperty("JETTY_REQUEST");
        int jettyRequestThreads = Integer.parseInt(jettySelectorStr == null ? Runtime.getRuntime().availableProcessors() + "" : jettyRequestStr);

        //is not concurrency then force request threads to be 1
        if(!config.isEnableConcurrency()){
            jettyRequestThreads = 1;
        }

        //calculate pool max, accept + select + request.
        int jettyPoolMax = jettyAcceptorThreads + jettySelectorThreads + jettyRequestThreads;

        //create server using given threadpool
        server = new Server(new QueuedThreadPool(jettyPoolMax, jettyPoolMax));
        server.setHandler(handler);
        ServerConnector connector = new ServerConnector(server, jettyAcceptorThreads, jettySelectorThreads);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        try {
            server.start();

            logger.info("Sandbox ready (build: v{} runtime: {}) --  Running on port: {}, metadata on port: {}, reading from path: '{}' with {} worker(s)",
                environment.getProperty("SANDBOX_VERSION",String.class, "?"),
                config.getRuntimeVersion(),
                port,
                config.getMetadataPort() == null ? "disabled" : config.getMetadataPort(),
                basePath.toAbsolutePath().toRealPath().toString(),
                jettyRequestThreads
            );

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
