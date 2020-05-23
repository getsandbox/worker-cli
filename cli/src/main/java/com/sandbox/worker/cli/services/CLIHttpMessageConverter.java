package com.sandbox.worker.cli.services;

import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.core.server.exceptions.ConverterException;
import com.sandbox.worker.core.server.services.HttpMessageConverter;
import io.micronaut.http.HttpRequest;

public class CLIHttpMessageConverter extends HttpMessageConverter {

    @Override
    public HttpRuntimeRequest processRequest(HttpRequest rawRequest, HttpRuntimeRequest request) throws ConverterException {
        //default for cli instance
        request.setSandboxId("sandbox-id");
        request.setFullSandboxId("sandbox-id");
        request.setSandboxName("sandbox");
        return super.processRequest(rawRequest, request);
    }

}
