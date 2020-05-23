package com.sandbox.worker.models.enums;


public enum RuntimeVersion {
    VERSION_1,
    VERSION_2,
    VERSION_3;

    public static RuntimeVersion getLatest(){
        return VERSION_3;
    }

    public static String toCLIString() {
        return VERSION_1 + "|" + VERSION_2;
    }
}
