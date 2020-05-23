package com.sandbox.worker.models;

public class ScriptSource {

    String path;
    int lineNumber;
    String implementation;
    String requestParameter = null;
    String responseParameter = null;

    public ScriptSource() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(String requestParameter) {
        this.requestParameter = requestParameter;
    }

    public String getResponseParameter() {
        return responseParameter;
    }

    public void setResponseParameter(String responseParameter) {
        this.responseParameter = responseParameter;
    }

}
