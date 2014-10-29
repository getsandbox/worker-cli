package com.sandbox.runtime.js.models;

/**
 * Created by drew on 3/08/2014.
 */
public class JSError {
    private String name;
    private String message;
    private String stackTrace;

    public JSError(String message, String stackTrace) {
        // default to SyntaxError
        this.name = "SyntaxError";
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public JSError(String name, String message, String stackTrace) {
        this.name = name;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
