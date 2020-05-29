package com.sandbox.worker.cli.services;

import com.sandbox.worker.core.server.exceptions.ConverterException;
import com.sandbox.worker.core.server.services.HttpMessageConverter;
import com.sandbox.worker.models.HttpRuntimeRequest;
import io.netty.handler.codec.http.FullHttpRequest;

public class CLIHttpMessageConverter extends HttpMessageConverter {

    @Override
    public HttpRuntimeRequest processRequest(FullHttpRequest rawRequest, HttpRuntimeRequest request) throws ConverterException {
        //default for cli instance
        request.setSandboxId("sandbox-id");
        request.setFullSandboxId("sandbox-id");
        request.setSandboxName("sandbox");
        return super.processRequest(rawRequest, request);
    }

}
