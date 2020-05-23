package com.sandbox.worker.core.services;

import com.sandbox.worker.models.interfaces.RepositoryArchiveService;

import java.io.File;
import java.io.InputStream;

public class LocalFileRepositoryArchiveService implements RepositoryArchiveService {

    private File repositoryBasePath;

    public LocalFileRepositoryArchiveService(File repositoryBasePath) {
        this.repositoryBasePath = repositoryBasePath;
    }

    @Override
    public void set(String fullSandboxId, InputStream inputStream) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean exists(String fullSandboxId) {
        return true;
    }

    @Override
    public InputStream getStream(String fullSandboxId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public File getUnpackedDirectory(String fullSandboxId) {
        return repositoryBasePath;
    }
}
