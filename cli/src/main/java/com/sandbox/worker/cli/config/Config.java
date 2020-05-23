package com.sandbox.worker.cli.config;

import com.sandbox.worker.models.enums.RuntimeVersion;
import io.micronaut.core.annotation.Introspected;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Introspected
@Command(name = "sandbox")
public class Config {

    @Parameters(index = "0", description = "Command to run (legacy support)", hidden = true, defaultValue = "run")
    private String legacyCommand;

    @Option(names = {"--port"}, description = "The port to listen on for requests")
    private int requestListenerPort = 8080;

    @Option(names = {"--metadataPort", "--activityPort"}, description = "The port to optionally start the activity api on, can be used to introspect what requests are hitting the server, useful for CI assertions")
    private int activityListenerPort = -1;

    @Option(names = {"--metadataLimit", "--activityLimit"}, description = "The number of activity messages to keep in-memory before they get discarded")
    private int activityStorageLimit = 100;

    @Option(names = {"--quiet"}, description = "Reduce logging, request / response and console.log() won't be shown, only errors.")
    private boolean quietLogging = false;

    @Option(names = {"--verbose"}, description = "Increase logging, request / response bodies will be shown")
    private boolean verboseLogging = false;

    @Option(names = {"--base"}, description = "The directory to try and load the Sandbox JS definition from")
    private File basePath = new File(".");

    @Option(names = {"--watch"}, description = "Whether to watch the base path for changes and automatically reload or not")
    private boolean watchBasePathForChanges = true;

    @Option(names = {"--state"}, description = "The file to load and store the Sandbox state object to, by default state will only exist ephemerally in-memory")
    private File statePath = null;

    @Option(names = {"--configProperties"}, hidden = true)
    private File configPath = null;

    @Option(names = {"--requestMaxContentLength"}, hidden = true)
    private int requestMaxContentLength = 1048576; //bytes

    @Option(names = {"--requestMaxRenderLength"}, hidden = true)
    private int requestMaxRenderLength = 1048576; //bytes

    @Option(names = {"--runtimeVersion"}, description = "The runtime version to execute at, the version effects what libraries are injected and what ECMAScript version is available")
    private RuntimeVersion version = RuntimeVersion.getLatest();

    public int getRequestListenerPort() {
        return requestListenerPort;
    }

    public void setRequestListenerPort(int requestListenerPort) {
        this.requestListenerPort = requestListenerPort;
    }

    public int getActivityListenerPort() {
        return activityListenerPort;
    }

    public void setActivityListenerPort(int metadataListenerPort) {
        this.activityListenerPort = metadataListenerPort;
    }

    public int getActivityStorageLimit() {
        return activityStorageLimit;
    }

    public void setActivityStorageLimit(int activityStorageLimit) {
        this.activityStorageLimit = activityStorageLimit;
    }

    public File getBasePath() {
        return basePath;
    }

    public void setBasePath(File basePath) {
        this.basePath = basePath;
    }

    public boolean isWatchBasePathForChanges() {
        return watchBasePathForChanges;
    }

    public void setWatchBasePathForChanges(boolean watchBasePathForChanges) {
        this.watchBasePathForChanges = watchBasePathForChanges;
    }

    public File getStatePath() {
        return statePath;
    }

    public void setStatePath(File statePath) {
        this.statePath = statePath;
    }

    public File getConfigPath() {
        return configPath;
    }

    public void setConfigPath(File configPath) {
        this.configPath = configPath;
    }

    public int getRequestMaxContentLength() {
        return requestMaxContentLength;
    }

    public void setRequestMaxContentLength(int requestMaxContentLength) {
        this.requestMaxContentLength = requestMaxContentLength;
    }

    public int getRequestMaxRenderLength() {
        return requestMaxRenderLength;
    }

    public void setRequestMaxRenderLength(int requestMaxRenderLength) {
        this.requestMaxRenderLength = requestMaxRenderLength;
    }

    public RuntimeVersion getVersion() {
        return version;
    }

    public void setVersion(RuntimeVersion version) {
        this.version = version;
    }

    public boolean isQuietLogging() {
        return quietLogging;
    }

    public void setQuietLogging(boolean quietLogging) {
        this.quietLogging = quietLogging;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }
}
