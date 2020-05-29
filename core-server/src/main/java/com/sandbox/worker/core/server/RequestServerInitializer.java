package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;

class RequestServerInitializer extends ChannelInitializer<SocketChannel> {

    private final RequestHandler requestFilter;
    private final ObjectMapper mapper;

    public RequestServerInitializer(RequestHandler requestFilter, ObjectMapper mapper) {
        this.requestFilter = requestFilter;
        this.mapper = mapper;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerExpectContinueHandler());
        //TODO set max request body length here, add test
        p.addLast(new HttpObjectAggregator(1 * 1024 * 1024));
        p.addLast(new RequestServerHandler(requestFilter, mapper));
    }
}
