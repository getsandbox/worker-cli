package com.sandbox.worker.test;

import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.InMemoryStateService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.RepositoryService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;

public class ProcessRequestExecutorHelper {

    public static SandboxIdentifier defaultSandboxIdentifier = new SandboxIdentifier("1", "1");

    public static WorkerScriptContext context(String pathOfFiles) throws Exception {
        return context(null, pathOfFiles, RuntimeVersion.VERSION_3);
    }

    public static WorkerScriptContext context(Class resourceTarget, String pathOfFiles) throws Exception {
        return context(resourceTarget, pathOfFiles, RuntimeVersion.VERSION_3);
    }

    public static WorkerScriptContext context(String pathOfFiles, RuntimeVersion version) throws Exception {
        return context(null, pathOfFiles, new InMemoryStateService(), version);
    }

    public static WorkerScriptContext context(Class resourceTarget, String pathOfFiles, RuntimeVersion version) throws Exception {
        return context(resourceTarget, pathOfFiles, new InMemoryStateService(), version);
    }

    public static WorkerScriptContext context(Class resourceTarget, String pathOfFiles, MetadataService metadataService) throws Exception {
        return context(resourceTarget, pathOfFiles, new InMemoryStateService(), metadataService);
    }

    public static WorkerScriptContext context(String pathOfFiles, MetadataService metadataService) throws Exception {
        return context(null, pathOfFiles, new InMemoryStateService(), metadataService);
    }

    public static WorkerScriptContext context(Class resourceTarget, String pathOfFiles, BufferingStateService stateService, RuntimeVersion version) throws Exception {
        return context(resourceTarget, pathOfFiles, stateService, new InMemoryMetadataService(version));
    }

    public static WorkerScriptContext context(String pathOfFiles, BufferingStateService stateService, RuntimeVersion version) throws Exception {
        return context(null, pathOfFiles, stateService, new InMemoryMetadataService(version));
    }

    public static WorkerScriptContext context(Class resourceTarget, String pathOfFiles, BufferingStateService stateService, MetadataService metadataService) throws Exception {
        return ContextFactory.createContext(
                defaultSandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(resourceTarget == null ? ProcessRequestExecutorHelper.class : resourceTarget, pathOfFiles)),
                stateService,
                metadataService
        );
    }

    public static String buildRepo(RepositoryService repositoryService, String... fileNames) throws IOException {
        Path tempDir = Files.createTempDirectory("test");
        for(String file : fileNames){
            Path resolvedPath = tempDir.resolve(file);
            FileUtils.writeStringToFile(resolvedPath.toFile(), repositoryService.getRepositoryFile("1", file), "UTF-8");
        }
        return tempDir.toFile().getAbsolutePath();
    }

    public static HttpRuntimeRequest buildHttp(String method, String url){
        return buildHttp(method, url, null);

    }

    public static HttpRuntimeRequest buildHttp(String method, String url, HashMap headers){
        HttpRuntimeRequest req = new HttpRuntimeRequest();
        req.setMethod(method);
        req.setUrl(url);

        if(headers == null) {
            req.setHeaders(new HashMap<>());
        }else{
            req.setHeaders(headers);
        }

        req.setSandboxId("1234");
        req.setSandboxName("testsandbox");
        req.setFullSandboxId("1234");
        req.setFullSandboxName("testsandbox");

        return req;
    }
}
