package com.sandbox.worker.core.js.models;

import com.sandbox.worker.core.graal.ResettableByteArrayOutputStream;
import com.sandbox.worker.core.js.ScriptFunctions;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.RoutingTable;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.SandboxMetadata;

import java.io.File;
import org.graalvm.polyglot.Context;

import static com.sandbox.worker.core.js.JSContextHelper.execute;
import static com.sandbox.worker.core.js.JSContextHelper.get;

public class WorkerScriptContext implements AutoCloseable {

    private RuntimeVersion runtimeVersion;
    private WorkerRunnableContext context;
    private File repositoryBasePath;
    private Context executionContext; // this is the graal context we execute in for a given sandbox
    private ResettableByteArrayOutputStream executionContextOutput; // this is the stream attached to the context for stdout and stderr
    private ScriptObject scriptObject; // this is base of the `Sandbox` obj in the context
    private ScriptFunctions scriptFunctions;
    private RoutingTable routingTable;
    private boolean needsBootstrap = true;
    private long lastExecutionTimestamp = System.currentTimeMillis();
    private SandboxMetadata metadata;

    public RuntimeVersion getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(RuntimeVersion runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public WorkerRunnableContext getContext() {
        return context;
    }

    public void setContext(WorkerRunnableContext context) {
        this.context = context;
    }

    public void setExecutionContext(Context executionContext) {
        this.executionContext = executionContext;
    }

    public Context getExecutionContext() {
        return executionContext;
    }

    public ResettableByteArrayOutputStream getExecutionContextOutput() {
        return executionContextOutput;
    }

    public void setExecutionContextOutput(ResettableByteArrayOutputStream executionContextOutput) {
        this.executionContextOutput = executionContextOutput;
    }

    public String getAndResetExecutionContextOutput(){
        String output = new String(getExecutionContextOutput().toByteArray()).trim();
        getExecutionContextOutput().reset();
        return output;
    }

    public ScriptObject getScriptObject() {
        return scriptObject;
    }

    public void setScriptObject(ScriptObject scriptObject) {
        this.scriptObject = scriptObject;
    }

    public void setScriptFunctions(ScriptFunctions scriptFunctions) {
        this.scriptFunctions = scriptFunctions;
    }

    public ScriptFunctions getScriptFunctions() {
        return scriptFunctions;
    }

    public SandboxIdentifier getSandboxIdentifier() {
        return context.getSandboxIdentifier();
    }

    public BufferingStateService getStateService() {
        return context.getStateService();
    }

    public File getRepositoryBasePath() {
        return repositoryBasePath;
    }

    public void setRepositoryBasePath(File repositoryBasePath) {
        this.repositoryBasePath = repositoryBasePath;
    }

    public boolean needsBootstrap() {
        return needsBootstrap;
    }

    public void setNeedsBootstrap(boolean needsBootstrap) {
        this.needsBootstrap = needsBootstrap;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void notifyPossibleStateChange() {
        //notify of change, get state obj from context and pass along as way to serialise it
        getStateService().notifyPossibleChange(
                getSandboxIdentifier().getSandboxId(), () -> execute(this, "JSON.stringify", get(this, "state")).asString()
        );
    }

    public void notifyExecution() {
        this.lastExecutionTimestamp = System.currentTimeMillis();
    }

    public long getLastExecutionTimestamp() {
        return lastExecutionTimestamp;
    }

    @Override
    public void close() {
        //close with force, assume we will manage the inflight executions to avoid the context being used while being closed.
        executionContext.close(true);
    }

    public void setMetadata(SandboxMetadata metadata) {
        this.metadata = metadata;
    }

    public SandboxMetadata getMetadata() {
        return metadata;
    }
}
