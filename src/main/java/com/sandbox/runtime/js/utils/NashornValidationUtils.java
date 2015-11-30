package com.sandbox.runtime.js.utils;

import java.util.Map;

/**
 * Created by drew on 4/08/2014.
 */
public class NashornValidationUtils extends NashornUtils {

    private Map<String, String> fileCache;

    public NashornValidationUtils() {
        super();
    }

    @Override
    public String readFile(String filename) {

        String fileContents = fileCache.get(filename);

        if (fileContents == null) {
            // TODO: do something
            return null;
        }

        return fileContents;
    }

    @Override
    public boolean hasFile(String filename) {
        return fileCache.containsKey(filename.trim());
    }

    public void setFileCache(Map<String, String> fileCache) {
        this.fileCache = fileCache;
    }
}

