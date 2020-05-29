package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Context
@Singleton
public class RequestServer {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServer.class);
    private final int listenerPort;
    private final RequestHandler requestFilter;
    private final ObjectMapper mapper;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channel;

    public RequestServer(@Value("${app.worker.port:8080}") int listenerPort, RequestHandler requestFilter, ObjectMapper mapper) {
        this.listenerPort = listenerPort;
        this.requestFilter = requestFilter;
        this.mapper = mapper;
    }

    @PostConstruct
    public void start() {
        // Configure the server.
        bossGroup = new NioEventLoopGroup(1, Executors.newSingleThreadExecutor());
        workerGroup = new NioEventLoopGroup(2, Executors.newFixedThreadPool(2));

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new RequestServerInitializer(requestFilter, mapper));
        channel = b.bind(listenerPort);
        onStart();
    }

    public void onStart() {
        LOG.info("Request server started on port: {}", listenerPort);
    }

    @PreDestroy
    public void stop() {
        LOG.info("Request server stopping..");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            //noop
        }
        LOG.info("Request server stopped");
    }
}

