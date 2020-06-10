package com.sandbox.worker.core.js;

import com.oracle.truffle.api.test.polyglot.AccessPredicate;
import com.oracle.truffle.api.test.polyglot.RestrictedFileSystem;
import com.sandbox.worker.core.graal.ResettableByteArrayOutputStream;
import com.sandbox.worker.core.js.models.ScriptObject;
import com.sandbox.worker.core.js.models.WorkerRunnableContext;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.js.models.WorkerVersionContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.InMemoryStateService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.core.services.LocalFileRepositoryService;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.RepositoryArchiveService;
import com.sandbox.worker.models.interfaces.RepositoryService;
import com.sandbox.worker.models.interfaces.SandboxMetadata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ContextFactory.class);

    private static final Engine commonEngine = Engine.newBuilder().useSystemProperties(false).build();
    private static final Map<SandboxIdentifier, WorkerScriptContext> existingContexts = new ConcurrentHashMap<>();
    private static final Map<SandboxIdentifier, Boolean> existingContextLocks = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "contextCleanup"));

    public static void prepareContext(){
        LOG.info("Preparing contexts..");
        BufferingStateService stateService = new InMemoryStateService("{}");
        for (RuntimeVersion version : RuntimeVersion.values()) {
            long start = System.currentTimeMillis();
            Path tempRepository = null;
            try {
                tempRepository = Files.createTempDirectory("prepare-context");
                FileUtils.write(tempRepository.resolve("main.js").toFile(), "", "UTF-8");
                WorkerScriptContext context = ContextFactory.createContext(
                        new SandboxIdentifier("prepare-context-" + version.name().toLowerCase()),
                        new LocalFileRepositoryArchiveService(tempRepository.toFile()),
                        stateService,
                        new InMemoryMetadataService(version)
                );
                context.close();
            } catch (Exception e) {
                LOG.error("Error preparing context", e);

            } finally {
                try {
                    if (tempRepository != null) {
                        FileUtils.deleteDirectory(tempRepository.toFile());
                    }
                } catch (IOException e) {
                    LOG.error("Error cleaning up", e);
                }
            }
            LOG.info("Prepared context for: {} took: {}ms", version, (System.currentTimeMillis() - start));
        }
    }

    private static Object getContextLock(SandboxIdentifier sandboxIdentifier) {
        synchronized (existingContextLocks) {
            return existingContextLocks.computeIfAbsent(sandboxIdentifier, k -> true);
        }
    }

    //find an existing context already created, or create one if none yet.
    public static WorkerScriptContext getOrCreateContext(SandboxIdentifier sandboxIdentifier, RepositoryArchiveService archiveService, BufferingStateService stateService, MetadataService metadataService) throws Exception {
        WorkerScriptContext scriptContext = null;

        //find existing key for identifier so we can synchronize on it, this means we can block retrieving a context while we are resetting it.
        Object contextLock = getContextLock(sandboxIdentifier);
        synchronized (contextLock) {
            //block getting an existing context, we might be resetting it atm, and we want the new one not the old one
            scriptContext = existingContexts.get(sandboxIdentifier);
            if (scriptContext == null) {
                scriptContext = createContext(sandboxIdentifier, archiveService, stateService, metadataService);
                existingContexts.put(sandboxIdentifier, scriptContext);
            }
        }

        scriptContext.notifyExecution();
        return scriptContext;
    }

    public static void removeContexts(Function<WorkerScriptContext, Boolean> filter) {
        List<WorkerScriptContext> entries = new ArrayList(existingContexts.values());
        LOG.info("Checked {} context entries", entries.size());
        entries.stream().filter(i -> filter.apply(i)).forEach(c -> {
            try {
                removeContextImmediately(c.getSandboxIdentifier());
            } catch (Exception e) {
                LOG.error("Error removing context", e);
            }
        });
    }

    public static void removeContext(SandboxIdentifier sandboxIdentifier) {
        existingContextLocks.remove(sandboxIdentifier);
        WorkerScriptContext context = existingContexts.remove(sandboxIdentifier);
        removeContext(context);
    }

    public static void removeContext(WorkerScriptContext context){
        if (context != null){
            cleanupExecutor.schedule(() -> {
                context.getStateService().flush(context.getSandboxIdentifier().getSandboxId());
                context.close();
            }, 20, TimeUnit.SECONDS);
        }
    }

    public static void removeContextImmediately(SandboxIdentifier sandboxIdentifier){
        existingContextLocks.remove(sandboxIdentifier);
        WorkerScriptContext removedContext = existingContexts.remove(sandboxIdentifier);
        if(removedContext != null){
            removedContext.getStateService().flush(removedContext.getSandboxIdentifier().getSandboxId());
            removedContext.close();
        }
    }

    public static void replaceExistingContext(String sandboxId, boolean flushBeforeCreate) throws Exception {
        Optional<WorkerScriptContext> existingContext = existingContexts.entrySet()
                .stream().filter(e -> e.getKey().getSandboxId().equals(sandboxId))
                .map(e -> e.getValue())
                .findFirst();
        if(existingContext.isPresent()) {
            replaceExistingContext(existingContext.get().getContext(), flushBeforeCreate);
        }
    }

    public static WorkerScriptContext replaceExistingContext(WorkerRunnableContext taskContext, boolean flushBeforeCreate) throws Exception {
        return replaceContext(taskContext.getSandboxIdentifier(), taskContext.getArchiveService(), taskContext.getStateService(), flushBeforeCreate, taskContext.getMetadataService());
    }

    public static WorkerScriptContext replaceExistingContext(WorkerRunnableContext taskContext) throws Exception {
        return replaceContext(taskContext.getSandboxIdentifier(), taskContext.getArchiveService(), taskContext.getStateService(), taskContext.getMetadataService());
    }

    //This will recreate a brand new context from the current definition,state,config etc
    public static WorkerScriptContext replaceContext(SandboxIdentifier sandboxIdentifier, RepositoryArchiveService archiveService, BufferingStateService stateService, MetadataService metadataService) throws Exception {
        return replaceContext(sandboxIdentifier, archiveService, stateService, true, metadataService);
    }

    public static WorkerScriptContext replaceContext(SandboxIdentifier sandboxIdentifier, RepositoryArchiveService archiveService, BufferingStateService stateService, boolean flushBeforeCreate, MetadataService metadataService) throws Exception {
        LOG.info("Replacing scriptContext for {}", sandboxIdentifier);
        WorkerScriptContext newScriptContext = null;

        if(flushBeforeCreate) {
            //flush state so its ready for new context load
            stateService.flush(sandboxIdentifier.getSandboxId());
        }
        //find existing key for identifier so we can synchronize on it, this means we can block retrieving a context while we are resetting it.
        Object contextLock = getContextLock(sandboxIdentifier);
        synchronized (contextLock) {
            //queue old script context for closure, about to be replaced but might be currently in use.
            WorkerScriptContext oldScriptContext = existingContexts.get(sandboxIdentifier);
            cleanupExecutor.schedule(() -> oldScriptContext.close(), 20, TimeUnit.SECONDS);

            //force create of new context, will pull all latest definitions, state etc
            newScriptContext = createContext(sandboxIdentifier, archiveService, stateService, metadataService);
            existingContexts.put(
                    sandboxIdentifier,
                    newScriptContext
            );
        }

        return newScriptContext;
    }

    public static WorkerScriptContext createContext(SandboxIdentifier sandboxIdentifier, RepositoryArchiveService archiveService, BufferingStateService stateService, MetadataService metadataService) throws Exception {
        LOG.debug("createContext - start");
        long start = System.currentTimeMillis();

        //get archive service to retrieve (if necessary) and unpack (if necessary) the repo files and provide the directory
        File repositoryBasePath = archiveService.getUnpackedDirectory(sandboxIdentifier.getFullSandboxId());
        LOG.debug("createContext - unpacked definition at: {}ms", System.currentTimeMillis() - start);

        //check that path is good, should be since archive service provided it
        if (repositoryBasePath == null || !repositoryBasePath.exists()) {
            throw new RuntimeException("Repository path isn't provided or doesn't exist");
        }
        //check that the repo details are good, both require() style and import {} style
        if (!Paths.get(repositoryBasePath.getAbsolutePath(), "main.js").toFile().exists() && !Paths.get(repositoryBasePath.getAbsolutePath(), "main.mjs").toFile().exists()) {
            throw new RuntimeException("Repository is missing 'main.js' or 'main.mjs'");
        }
        //canonicalize and make absolute, no relative nonsense.
        repositoryBasePath = repositoryBasePath.getCanonicalFile().getAbsoluteFile();

        //take unpacked directory and wrap it with a repo service and restricted fs to enforce access
        RepositoryService repositoryService = new LocalFileRepositoryService(repositoryBasePath);
        Path workingDirectory = repositoryBasePath.getCanonicalFile().getAbsoluteFile().toPath();
        RestrictedFileSystem fileSystem = new RestrictedFileSystem(
                newFullIOFileSystem(workingDirectory),
                workingDirectory,
                new AccessPredicate(Arrays.asList(workingDirectory)),
                new AccessPredicate(Collections.emptyList())
        );
        LOG.debug("createContext - created filesystem at: {}ms", System.currentTimeMillis() - start);

        //build a new graaljs context, turn off most of the things
        ResettableByteArrayOutputStream contextOutput = new ResettableByteArrayOutputStream();

        //get config, for properties and runtime version
        SandboxMetadata metadata = metadataService.getMetadata(sandboxIdentifier.getSandboxId());
        RuntimeVersion version = metadata.getRuntimeVersion();
        WorkerVersionContext versionContext = WorkerVersionContext.get(version);

        Context.Builder contextBuilder = Context.newBuilder("js");

        //filesystem is optional for warmup
        if(fileSystem != null){
            contextBuilder.fileSystem(fileSystem);
        }

        contextBuilder
                .engine(commonEngine)
                .out(contextOutput)
                .err(contextOutput)
                .allowHostAccess(HostAccess.EXPLICIT)
                .allowCreateThread(false)
                .allowNativeAccess(false)
                .allowAllAccess(false)
                .allowHostClassLoading(false)
                .allowIO(true)
                .allowExperimentalOptions(true)
                .option("js.polyglot-builtin", "false")
                .option("js.shared-array-buffer", "false")
                .option("js.atomics", "false")
                .option("js.agent-can-block", "false")
                .option("js.java-package-globals", "false")
                .option("js.performance", "false")
                .option("js.print", "true")
                .option("js.load", "true") //require() style needs load() otherwise have to do readFile and eval which ends up anonymous
                //.option("js.experimental-foreign-object-prototype", "true")
                .option("js.graal-builtin", "false");

        //process optional option() overrides from version
        if (versionContext.getOptionOverrides() != null && !versionContext.getOptionOverrides().isEmpty()) {
            versionContext.getOptionOverrides().entrySet().stream().forEach(e -> contextBuilder.option(e.getKey(), e.getValue()));
        }

        Context executionContext = contextBuilder.build();
        LOG.debug("createContext - created execution context at: {}ms", System.currentTimeMillis() - start);

        //patch internal things
        ScriptFunctions scriptFunctions = new ScriptFunctions(sandboxIdentifier, repositoryService);
        JSContextHelper.put(executionContext, "__service", scriptFunctions);

        ScriptObject scriptObject = new ScriptObject();
        JSContextHelper.put(executionContext, "__mock", scriptObject);

        //add async common libraries
        String sandboxLibrariesJS = IOUtils.toString(ContextFactory.class.getClassLoader().getResource(versionContext.getLibrariesPath() + "sandbox-libraries.js"), "UTF-8");
        executionContext.eval(Source.newBuilder("js", sandboxLibrariesJS, "<sandbox-libraries>").build());

        String sandboxTemplatesJS = IOUtils.toString(ContextFactory.class.getClassLoader().getResource(versionContext.getLibrariesPath() + "sandbox-templates.js"), "UTF-8");
        executionContext.eval(Source.newBuilder("js", sandboxTemplatesJS, "<sandbox-templates>").build());

        String sandboxValidatorJS = IOUtils.toString(ContextFactory.class.getClassLoader().getResource(versionContext.getLibrariesPath() + "sandbox-validator.js"), "UTF-8");
        executionContext.eval(Source.newBuilder("js", sandboxValidatorJS, "<sandbox-validator>").build());

        String sandboxPatchJS = IOUtils.toString(ContextFactory.class.getClassLoader().getResource(versionContext.getLibrariesPath() + "sandbox-patch.js"), "UTF-8");
        executionContext.eval(Source.newBuilder("js", sandboxPatchJS, "<sandbox-internal>").build());

        LOG.debug("createContext - patched context at: {}ms", System.currentTimeMillis() - start);

        //inject state
        String stateJson = stateService.getSandboxState(sandboxIdentifier.getSandboxId());
        JSContextHelper.put(executionContext, "__stateJson", stateJson);
        JSContextHelper.eval(executionContext, "state = JSON.parse(__stateJson)");
        JSContextHelper.remove(executionContext, "__stateJson");
        LOG.debug("createContext - injected state at: {}ms", System.currentTimeMillis() - start);

        //inject read-only config properties
        JSContextHelper.put(executionContext, "__config", ProxyObject.fromMap((Map)metadata.getConfig()));
        JSContextHelper.eval(executionContext, "Sandbox.config = __config");
        JSContextHelper.remove(executionContext, "__config");
        LOG.debug("createContext - injected config at: {}ms", System.currentTimeMillis() - start);

        WorkerScriptContext scriptContext = new WorkerScriptContext();
        scriptContext.setRuntimeVersion(version);
        scriptContext.setContext(new WorkerRunnableContext(
                sandboxIdentifier,
                archiveService,
                metadataService,
                stateService
        ));
        scriptContext.setExecutionContext(executionContext);
        scriptContext.setExecutionContextOutput(contextOutput);
        scriptContext.setScriptFunctions(scriptFunctions);
        scriptContext.setScriptObject(scriptObject);
        scriptContext.setRepositoryBasePath(repositoryBasePath);
        scriptContext.setMetadata(metadata);
        LOG.info("createContext for {} - done at: {}ms", sandboxIdentifier, System.currentTimeMillis() - start);

        return scriptContext;
    }

    protected static FileSystem newFullIOFileSystem(final Path workDir) {
        try {
            final Class<?> clz = Class.forName("com.oracle.truffle.polyglot.FileSystems");
            final Method m = clz.getDeclaredMethod("newDefaultFileSystem", Path.class);
            m.setAccessible(true);
            return (FileSystem) m.invoke(null, workDir);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
