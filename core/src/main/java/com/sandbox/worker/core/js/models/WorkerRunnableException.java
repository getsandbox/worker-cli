package com.sandbox.worker.core.js.models;

public class WorkerRunnableException extends Exception {

    public WorkerRunnableException() {
    }

    public WorkerRunnableException(String message) {
        super(message);
    }

    public WorkerRunnableException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkerRunnableException(Throwable cause) {
        super(cause);
    }

    public WorkerRunnableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
