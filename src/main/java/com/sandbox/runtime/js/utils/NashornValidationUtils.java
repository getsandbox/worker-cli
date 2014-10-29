package com.sandbox.runtime.js.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * Created by drew on 4/08/2014.
 */
public class NashornValidationUtils implements INashornUtils {

    @Autowired
    private ObjectMapper mapper;

    private static Logger logger = LoggerFactory.getLogger(NashornRuntimeUtils.class);

    private HashMap<String, String> fileCache;

    @Override
    public String jsonStringify(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("Error in jsonStringify with obj: " + o,e);
            return null;
        }
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

    public void setFileCache(HashMap<String, String> fileCache) {
        this.fileCache = fileCache;
    }
}

