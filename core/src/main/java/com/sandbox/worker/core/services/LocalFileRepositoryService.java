package com.sandbox.worker.core.services;

import com.sandbox.worker.models.interfaces.RepositoryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileRepositoryService implements RepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileRepositoryService.class);

    private File basePath;

    public LocalFileRepositoryService(File basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getRepositoryFile(String fullSandboxId, String filename) {
        if(Files.exists(basePath.toPath().resolve(filename))){
            try {
                String result = FileUtils.readFileToString(basePath.toPath().resolve(filename).toFile(), "UTF-8");
                return result;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean hasRepositoryFile(String fullSandboxId, String filename) {
        return (Files.exists(basePath.toPath().resolve(filename)));
    }

}
