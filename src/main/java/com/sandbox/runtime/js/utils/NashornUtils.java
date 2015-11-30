package com.sandbox.runtime.js.utils;

/**
 * Created by nickhoughton on 22/09/2014.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class NashornUtils {

    @Autowired
    private ObjectMapper mapper;

    protected  static Logger logger = LoggerFactory.getLogger(NashornUtils.class);

    public NashornUtils() {
    }

    public String doJsonStringify(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("Error in jsonStringify with obj: " + o, e);
            return null;
        }
    }

    //dumb but necessary because of nashorn's auto magic typing nonsense.
    public String jsonStringify(Object o) {
        return doJsonStringify(o);
    }

    public String jsonStringify(ScriptObject o) {
        return doJsonStringify(o);
    }

    public String jsonStringify(NativeArray o) {
        return doJsonStringify(o);
    }

    public abstract String readFile(String filename);

    public abstract boolean hasFile(String filename);
}
