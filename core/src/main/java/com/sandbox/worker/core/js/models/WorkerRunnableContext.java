package com.sandbox.worker.core.js.models;

import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.RepositoryArchiveService;

public class WorkerRunnableContext {

    private SandboxIdentifier sandboxIdentifier;
    private RepositoryArchiveService archiveService;
    private MetadataService metadataService;
    private BufferingStateService stateService;

    public WorkerRunnableContext(SandboxIdentifier sandboxIdentifier, RepositoryArchiveService archiveService, MetadataService metadataService, BufferingStateService stateService) {
        this.sandboxIdentifier = sandboxIdentifier;
        this.metadataService = metadataService;
        this.stateService = stateService;
        this.archiveService = archiveService;
    }

    public SandboxIdentifier getSandboxIdentifier() {
        return sandboxIdentifier;
    }

    public RepositoryArchiveService getArchiveService() {
        return archiveService;
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    public BufferingStateService getStateService() {
        return stateService;
    }

    public WorkerScriptContext getSandboxScriptContext() throws Exception {
        return ContextFactory.getOrCreateContext(sandboxIdentifier, archiveService, stateService, metadataService);
    }
}
