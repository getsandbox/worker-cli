package com.sandbox.worker.core.server.exceptions;

import com.sandbox.worker.models.enums.ErrorStrategyEnum;
import io.netty.handler.codec.http.HttpResponseStatus;

public class ServiceOverrideException extends Exception {
    private HttpResponseStatus status;
    private ErrorStrategyEnum errorStrategy;

    public ServiceOverrideException(String message, HttpResponseStatus status, ErrorStrategyEnum errorStrategy) {
        super(message);
        this.status = status;
        this.errorStrategy = errorStrategy;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public ErrorStrategyEnum getErrorStrategy() {
        return errorStrategy;
    }
}
