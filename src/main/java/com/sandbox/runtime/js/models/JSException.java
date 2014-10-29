package com.sandbox.runtime.js.models;

/**
 * Created by drew on 5/08/2014.
 */

import jdk.nashorn.internal.runtime.ParserException;

import javax.script.ScriptException;

public class JSException {

    private static final String REFERENCE_ERROR = "REFERENCE_ERROR";
    private static final String TYPE_ERROR = "TYPE_ERROR";
    private static final String RANGE_ERROR = "RANGE_ERROR";
    private static final String SYNTAX_ERROR = "SYNTAX_ERROR";

    private final ScriptException e;

    public JSException(ScriptException e) {
        this.e = e;
    }

    // TODO: could make this all static??
    public boolean isReferenceError() {
        return checkErrorType(REFERENCE_ERROR);
    }

    public boolean isTypeError() {
        return checkErrorType(TYPE_ERROR);
    }

    public boolean isRangeError() {
        return checkErrorType(RANGE_ERROR);
    }

    public boolean isSyntaxError() {
        return checkErrorType(SYNTAX_ERROR);
    }

    public boolean checkErrorType(String errorType) {
        if (e.getCause() instanceof ParserException) {
            ParserException ex = (ParserException)e.getCause();
            return (errorType.equalsIgnoreCase(ex.getErrorType().name()));
        }
        return false;
    }
}
