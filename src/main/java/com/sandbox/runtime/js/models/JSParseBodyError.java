package com.sandbox.runtime.js.models;

/**
 * Created by drew on 3/08/2014.
 */
public class JSParseBodyError extends JSError {

    private boolean isInvalid = true;
    private String raw;

    public boolean isInvalid() {
        return isInvalid;
    }

    public String getRaw() {
        return raw;
    }

    public JSParseBodyError(String message, String stackTrace, String body) {
        super("ParseError", message, stackTrace);
        this.raw = body;
    }
}
