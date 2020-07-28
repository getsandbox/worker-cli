package com.sandbox.worker.cli.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.cli.config.Config;
import com.sandbox.worker.core.services.FileBasedActivityStore;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Introspected;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

//This sucks, micronaut only supports 1 server at a time atm so we stand up a separate netty server to serve activity
//hopefully this can die soon and be replaced by some micronaut OTB
@Introspected
@Context
@Singleton
public class ActivityStoreServer {

    private Config config;

    private FileBasedActivityStore activityStore;

    private ObjectMapper mapper;

    @Inject
    public ActivityStoreServer(Config config, FileBasedActivityStore activityStore, ObjectMapper mapper) {
        this.config = config;
        this.activityStore = activityStore;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        //start if port and limit are configured
        if (config.getActivityStorageLimit() > 0 && config.getActivityListenerPort() > 0) {
            start();
        }
    }

    public void start() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, Executors.newSingleThreadExecutor());
        EventLoopGroup workerGroup = new NioEventLoopGroup(2, Executors.newFixedThreadPool(2));

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ActivityServerInitializer(mapper, activityStore));

        b.bind(config.getActivityListenerPort());
    }
}

@Introspected
class ActivityServerInitializer extends ChannelInitializer<SocketChannel> {

    private ObjectMapper mapper;
    private FileBasedActivityStore activityStore;

    public ActivityServerInitializer(ObjectMapper mapper, FileBasedActivityStore activityStore) {
        this.mapper = mapper;
        this.activityStore = activityStore;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new ActivityServerHandler(mapper, activityStore));
    }
}

@Introspected
class ActivityServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private ObjectMapper mapper;
    private FileBasedActivityStore activityStore;

    public ActivityServerHandler(ObjectMapper mapper, FileBasedActivityStore activityStore) {
        this.mapper = mapper;
        this.activityStore = activityStore;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws IOException {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            List<File> messages = activityStore.getAllAsFiles();

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            HttpResponse response = new DefaultHttpResponse(req.protocolVersion(), OK);
            response.headers().set(CONTENT_TYPE, APPLICATION_JSON);
            response.headers().set(CONTENT_LENGTH, messages.stream().map(f -> f.length() + 1).collect(Collectors.summingLong(v -> v)).longValue() + 1);

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, CLOSE);
            }

            ctx.write(response);

            ctx.write(Unpooled.wrappedBuffer("[".getBytes()), ctx.newProgressivePromise());
            for (int i = 0; i < messages.size(); i++) {
                File messageFile = messages.get(i);
                RandomAccessFile raf = new RandomAccessFile(messageFile, "r");
                ctx.write(new DefaultFileRegion(raf.getChannel(), 0, raf.length()), ctx.newProgressivePromise());
                if (i < messages.size() - 1){
                    ctx.write(Unpooled.wrappedBuffer(",".getBytes()), ctx.newProgressivePromise());
                }
            }
            ctx.write(Unpooled.wrappedBuffer("]".getBytes()), ctx.newProgressivePromise());
            ChannelFuture f = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
