package com.sandbox.worker.core.js.models;

public enum BodyContentType {
    JSON("json"),
    XML("xml"),
    URLENCODED("urlencoded"),
    FORMDATA("formdata"),
    UNKNOWN("unknown");

    String type;

    BodyContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
