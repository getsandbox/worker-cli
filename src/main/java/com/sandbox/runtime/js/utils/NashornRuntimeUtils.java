package com.sandbox.runtime.js.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.runtime.models.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by drew on 4/08/2014.
 */
public class NashornRuntimeUtils implements INashornUtils{

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    Cache cache;

    private static Logger logger = LoggerFactory.getLogger(NashornRuntimeUtils.class);

    private final String fullSandboxId;

    public NashornRuntimeUtils(String fullSandboxId) {
        this.fullSandboxId = fullSandboxId;
    }

    public String getFullSandboxId() { return fullSandboxId; }

    public String jsonStringify(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("Error in jsonStringify with obj: " + o,e);
            return null;
        }
    }

    public String readFile(String filename) {
        String fileContents;
        fileContents = cache.getRepositoryFile(fullSandboxId, filename);

        if (fileContents == null) {
            // TODO: do something
            return null;
        }

        return fileContents;
    }

    public boolean hasFile(String filename) {
        logger.debug("hasFile (%1$s) - %2$s", fullSandboxId,filename);
        return cache.hasRepositoryFile(fullSandboxId, filename);
    }
}
