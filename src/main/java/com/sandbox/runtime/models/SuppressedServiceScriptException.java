package com.sandbox.runtime.models;

/**
 * Created by drew on 7/08/2014.
 */
public class SuppressedServiceScriptException extends ServiceScriptException {

    public SuppressedServiceScriptException() {
    }

    public SuppressedServiceScriptException(Exception cause, String filename, int line, int column) {
        super(cause, filename, line, column);
    }

    public SuppressedServiceScriptException(String message) {
        super(message);
    }

    public SuppressedServiceScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public SuppressedServiceScriptException(Throwable cause) {
        super(cause);
    }

    public SuppressedServiceScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}