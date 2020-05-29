package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.worker.core.server.exceptions.ServiceOverrideException;
import com.sandbox.worker.models.Error;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Arrays;

public class ExceptionResponseSupport {

    public static FullHttpResponse writeExceptionToResponse(ObjectMapper mapper, Exception exception) {
        if (exception instanceof ServiceOverrideException) {
            ServiceOverrideException soe = (ServiceOverrideException) exception;
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    new HttpResponseStatus(soe.getStatus().code(), soe.getStatus().reasonPhrase() == null ? "" : soe.getStatus().reasonPhrase()),
                    Unpooled.wrappedBuffer(exception.getMessage().getBytes()));
        } else {
            return writeExceptionToResponse(mapper, HttpResponseStatus.INTERNAL_SERVER_ERROR, null, exception);
        }
    }

    public static FullHttpResponse writeExceptionToResponse(ObjectMapper mapper, HttpResponseStatus status, String reason, Exception exception) {
        ObjectNode errorWrapper = mapper.createObjectNode();
        ObjectNode error = mapper.convertValue(new Error(exception.getMessage()), ObjectNode.class);
        errorWrapper.set("errors", mapper.convertValue(Arrays.asList(error), ArrayNode.class));

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                new HttpResponseStatus(status.code(), reason == null ? "" : reason),
                Unpooled.wrappedBuffer(errorWrapper.toString().getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        return response;
    }
}
