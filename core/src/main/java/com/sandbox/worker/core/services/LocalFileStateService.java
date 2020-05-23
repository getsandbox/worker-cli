package com.sandbox.worker.core.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileStateService extends AbstractBufferingStateService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileStateService.class);

    private File statePath;

    public LocalFileStateService(File statePath) {
        this.statePath = statePath;
        super.start(15, 15);
    }

    @Override
    public String getSandboxState(String sandboxId) {
        String state = "{}";
        //load state if it exists and is correct
        if(statePath != null){
            Path stateFilePath = statePath.toPath();
            if(!Files.exists(stateFilePath) || statePath.length() == 0L){
                LOG.warn("State path has been specified, but the '{}' file does not exist, creating..", stateFilePath);
                setSandboxState(sandboxId, state);
            }else {
                try {
                    LOG.info("Loading state from '{}'", stateFilePath);
                    state = FileUtils.readFileToString(stateFilePath.toFile(), "UTF-8");
                } catch (IOException e) {
                    LOG.error("Error reading persisted state, ignoring..", e);
                }
            }
        }
        return state;
    }

    @Override
    public void setSandboxState(String sandboxId, String state) {
        Path stateFilePath = statePath.toPath();
        try {
            FileUtils.writeStringToFile(stateFilePath.toFile(), state, "UTF-8");
        } catch (IOException e) {
            LOG.error("Error writing state to file '{}'", stateFilePath.toFile().getAbsolutePath());
        }
    }

}
