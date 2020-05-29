package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RequestServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServerHandler.class);
    private final RequestHandler requestFilter;
    private final ObjectMapper mapper;

    public RequestServerHandler(RequestHandler requestFilter, ObjectMapper mapper) {
        this.requestFilter = requestFilter;
        this.mapper = mapper;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //map msg request
        request.retain();
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        requestFilter.submitRequest(((InetSocketAddress) remoteAddress).getHostName(), request, r -> {
            ReferenceCountUtil.safeRelease(request.release());
            if (HttpUtil.isKeepAlive(request)) {
                r.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            sendResponse(ctx, r);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("exceptionCaught", cause);
        sendResponse(ctx, ExceptionResponseSupport.writeExceptionToResponse(mapper, new Exception("Internal error")));
    }

    protected void sendResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ChannelFuture responseFuture = ctx.writeAndFlush(response);
        responseFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOG.error("Error writing response", future.cause());
            }
        });
    }

}
