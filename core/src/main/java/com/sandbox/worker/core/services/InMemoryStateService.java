package com.sandbox.worker.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryStateService extends AbstractBufferingStateService {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryStateService.class);

    private String state = "{}";

    public InMemoryStateService() {
    }

    public InMemoryStateService(String state) {
        this.state = state;
    }

    @Override
    public String getSandboxState(String sandboxId) {
        return state;
    }

    @Override
    public void setSandboxState(String sandboxId, String state) {
        this.state = state;
    }

}
