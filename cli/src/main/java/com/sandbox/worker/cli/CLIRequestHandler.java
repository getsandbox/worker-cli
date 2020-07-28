package com.sandbox.worker.cli;

import com.sandbox.worker.cli.config.Config;
import com.sandbox.worker.cli.services.LocalFileMetadataService;
import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.models.WorkerRunnableContext;
import com.sandbox.worker.core.server.RequestHandler;
import com.sandbox.worker.core.server.WorkerRequestRunnable;
import com.sandbox.worker.core.server.services.HttpMessageConverter;
import com.sandbox.worker.core.services.InMemoryStateService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.core.services.LocalFileStateService;
import com.sandbox.worker.core.utils.FormatUtils;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.RuntimeResponse;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.HTTPRoute;
import com.sandbox.worker.models.interfaces.SandboxEventEmitterService;
import com.sun.nio.file.SensitivityWatchEventModifier;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.scheduling.TaskExecutors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

//This is a filter as micronaut doesnt appear to have a way to have a wildcard route defined using normal syntax.
@Introspected
@Context
public class CLIRequestHandler extends RequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CLIRequestHandler.class);

    private final ExecutorService engineExecutor = Executors.newFixedThreadPool(1, r -> new Thread(r, "engine-executor"));
    private final SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("sandbox-id");
    private final Config config;
    private final FormatUtils formatUtils = new FormatUtils();

    public CLIRequestHandler(Config config, SandboxEventEmitterService eventEmitterService, HttpMessageConverter httpMessageConverter, @javax.inject.Named(TaskExecutors.IO) ExecutorService ioExecutor) {
        super(eventEmitterService,
                new ProcessRequestExecutor(config.getRequestMaxRenderLength()),
                config.getStatePath() == null ? new InMemoryStateService() : new LocalFileStateService(config.getStatePath()),
                new LocalFileMetadataService(config),
                new LocalFileRepositoryArchiveService(config.getBasePath()),
                httpMessageConverter,
                ioExecutor
        );
        this.config = config;

        //refresh when files on disk change
        setupRepositoryFileWatcher();

        //persist state locally when shutting down
        if (!(getStateService() instanceof InMemoryStateService)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Shutdown triggered, persisting state..");
                getStateService().flush();
            }));
        }
    }

    //Start housekeeping processes to complete delayed processes
    private void setupRepositoryFileWatcher() {
        //This doesnt work in graal appr
        if (config.isWatchBasePathForChanges()) {
            try {
                final WatchService watcher = FileSystems.getDefault().newWatchService();
                final SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.MEDIUM);
                        return FileVisitResult.CONTINUE;
                    }
                };

                Files.walkFileTree(config.getBasePath().toPath(), fileVisitor);
                new Thread(() -> {
                    try {
                        WatchKey key = watcher.take();
                        while (key != null) {
                            boolean reloaded = false;
                            //if js file has changed, clear routing table
                            for (WatchEvent event : key.pollEvents()) {
                                //checked reloaded flag so we don't reload N times for a set of related changes
                                if (reloaded == false && event.context().toString().endsWith(".js") || event.context().toString().endsWith(".mjs")) {
                                    reloaded = true;
                                    LOG.info("Reloading definition.. {} changed", event.context().toString());
                                    try {
                                        ContextFactory.replaceContext(sandboxIdentifier, getRepositoryArchiveService(), getStateService(), getMetadataService());
                                    } catch (Exception e) {
                                        LOG.error("Error reloading definition", e);
                                    }
                                }
                            }
                            //reset reloaded flag so next time a change comes in its actioned
                            reloaded = false;

                            //cleanup
                            key.reset();
                            key = watcher.take();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void handleEngineRequest(SandboxIdentifier sandboxIdentifier, WorkerRequestRunnable runnable) {
        runnable.setTaskContext(new WorkerRunnableContext(sandboxIdentifier, getRepositoryArchiveService(), getMetadataService(), getStateService()));
        engineExecutor.submit(runnable);
    }

    @Override
    protected void logRequest(HttpRuntimeRequest request, HTTPRoute matchedRouteDetails) {
        if (config.isQuietLogging()) return;

        String matchedRouteDescription = "No matching route";
        if (matchedRouteDetails != null) matchedRouteDescription = "Matched route '" + matchedRouteDetails.getPath() + "'";

        String bodyDescription = "No body found";
        if (!StringUtils.isEmpty(request.getBody())) {
            String truncatedBody = renderBody(request.getBody(), request.getHeaders());
            bodyDescription = "Body: '" + truncatedBody + "' (" + truncatedBody.length() + " bytes)";
        }

        LOG.info("\n>> HTTP {} {}  ({})\n" +
                        ">> Headers: {}\n" +
                        ">> {}",
                request.getMethod(), request.getRawUrl(), matchedRouteDescription, getSafe(request.getHeaders(), new HashMap<>()), bodyDescription);

    }

    @Override
    protected void logConsole(String output) {
        if (config.isQuietLogging() || output == null) return;

        if (output.endsWith("\n")) output = output.substring(0, output.length() - 2);
        LOG.info(output);
    }

    @Override
    protected void logResponse(HttpRuntimeRequest request, HttpRuntimeResponse response, long startTime, RuntimeVersion runtimeVersion) {
        if (config.isQuietLogging()) return;

        //then response
        String bodyDescription = "No body found";
        if (!StringUtils.isEmpty(response.getBody())) {
            String truncatedBody = renderBody(response.getBody(), response.getHeaders());
            bodyDescription = "Body: '" + truncatedBody + "'";
        }

        if (response instanceof RuntimeResponse) {
            LOG.info("<< Status: {} (processing {}ms wallclock {}ms)\n" +
                            "<< Headers: {}\n" +
                            "<< {}",
                    ((HttpRuntimeResponse) response).getStatusCode(),
                    response.getDurationMillis(),
                    (response.getRespondedTimestamp() - startTime),
                    getSafe(response.getHeaders(),
                            new HashMap<>()),
                    bodyDescription
            );
        }
    }

    private String renderBody(String body, Map<String, String> headers) {

        if (config.isVerboseLogging()) {
            if (formatUtils.isXml(headers)) return formatUtils.formatXml(body);
            return body;
        } else {
            body = body.replace('\n', ' ');
            if (body.length() > 150) {
                return body.substring(0, 150) + "...";
            } else {
                return body;
            }
        }
    }

    private <T> T getSafe(T obj, T defaultValue) {
        if (obj == null) return defaultValue;
        return obj;
    }

}
