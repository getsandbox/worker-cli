package com.sandbox.runtime.js.utils;

/**
 * Created by nickhoughton on 22/09/2014.
 */
public interface INashornUtils {
    String jsonStringify(Object o);

    String readFile(String filename);

    boolean hasFile(String filename);
}
