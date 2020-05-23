package com.sandbox.worker.core.js;

import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.RepositoryService;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.HostAccess;

@HostAccess.Implementable
public class ScriptFunctions {

    private final SandboxIdentifier sandboxIdentifier;
    private final RepositoryService repositoryService;

    public ScriptFunctions(SandboxIdentifier sandboxIdentifier, RepositoryService repositoryService) {
        this.sandboxIdentifier = sandboxIdentifier;
        this.repositoryService = repositoryService;
    }

    @HostAccess.Export
    public String readFile(String filename) {
        return repositoryService.getRepositoryFile(sandboxIdentifier.getFullSandboxId(), filename);
    }

    @HostAccess.Export
    public boolean hasFile(String filename) {
        return repositoryService.hasRepositoryFile(sandboxIdentifier.getFullSandboxId(), filename);
    }

    @HostAccess.Export
    public String readLibrary(String version, String libraryName) throws IOException {
        return IOUtils.toString(ContextFactory.class.getClassLoader().getResource("com/sandbox/runtime/v2/js/" + version + "/" + libraryName).openStream(), "UTF-8");
    }

}
