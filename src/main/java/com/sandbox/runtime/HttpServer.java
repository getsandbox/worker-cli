package com.sandbox.runtime;

import com.sandbox.runtime.config.Config;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
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
    Config config;

    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public void start() {
        int port = config.getHttpPort();
        Path basePath = config.getBasePath();

        startServer(port, basePath);
    }

    public void start(int port, Path basePath) {
        startServer(port, basePath);
    }

    private void startServer(int port, Path basePath) {
        String jettyAcceptorStr = System.getProperty("JETTY_ACCEPTOR");
        int jettyAcceptor = Integer.parseInt(jettyAcceptorStr == null ? "-1" : jettyAcceptorStr);
        String jettySelectorStr = System.getProperty("JETTY_SELECTOR");
        int jettySelector = Integer.parseInt(jettySelectorStr == null ? "-1" : jettySelectorStr);

        ThreadPool jettyThreadPool;
        if(System.getProperty("JETTY_MIN_THREADS") != null && System.getProperty("JETTY_MIN_THREADS") != null){
            jettyThreadPool = new QueuedThreadPool(Integer.parseInt(System.getProperty("JETTY_MIN_THREADS")), Integer.parseInt(System.getProperty("JETTY_MAX_THREADS")));
        }else{
            jettyThreadPool = new QueuedThreadPool();
        }

        server = new Server(jettyThreadPool);
        ServerConnector connector=new ServerConnector(server, jettyAcceptor, jettySelector);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(handler);

        try {
            server.start();

            logger.info("Sandbox ready (build: v{} runtime: {}) --  Running on port: {} with path: '{}'", environment.getProperty("SANDBOX_VERSION",String.class, "?"), config.getRuntimeVersion(), port, basePath.toAbsolutePath().toRealPath().toString());

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
