package com.sandbox.worker.core.server.exceptions;

import com.sandbox.worker.models.enums.ErrorStrategyEnum;
import io.micronaut.http.HttpStatus;

public class ServiceOverrideException extends Exception {
    private HttpStatus status;
    private ErrorStrategyEnum errorStrategy;

    public ServiceOverrideException(String message, HttpStatus status, ErrorStrategyEnum errorStrategy) {
        super(message);
        this.status = status;
        this.errorStrategy = errorStrategy;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorStrategyEnum getErrorStrategy() {
        return errorStrategy;
    }
}
