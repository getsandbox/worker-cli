package com.sandbox.runtime.js.utils;

import com.sandbox.runtime.models.Cache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by drew on 4/08/2014.
 */
public class NashornRuntimeUtils extends NashornUtils {

    @Autowired
    private Cache cache;

    private final String fullSandboxId;

    public NashornRuntimeUtils(String fullSandboxId) {
        this.fullSandboxId = fullSandboxId;
    }

    public String getFullSandboxId() {
        return fullSandboxId;
    }

    public String readFile(String filename) {
        return cache.getRepositoryFile(getFullSandboxId(), filename);
    }

    public boolean hasFile(String filename) {
        logger.debug("hasFile ({}) - {}", getFullSandboxId(),filename);
        return cache.hasRepositoryFile(getFullSandboxId(), filename);
    }
}
