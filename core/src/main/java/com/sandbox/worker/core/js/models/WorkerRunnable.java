package com.sandbox.worker.core.js.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

public abstract class WorkerRunnable implements Callable<Void> {

    protected static final Logger LOG = LoggerFactory.getLogger(WorkerRunnable.class);

    protected WorkerRunnableContext taskContext;

    //the thread processing this task, used to count execution time.
    private WeakReference<Thread> processingThread = null;

    //the amount of time for the request has breached the limit, the consumer has got a timeout / error.
    private boolean expired = false;
    private long createdTimestamp = System.currentTimeMillis();

    //the JS processing time for this request has exceeded the limit, it has been terminated.
    private boolean exceeded = false;

    @Override
    public Void call() throws Exception {
        try {
            //request has expired while its been waiting processing
            if (isExpired()) throw new WorkerRunnableException("Request has timed out");
            processingThread = new WeakReference(Thread.currentThread());

            doRun();

        } catch (Throwable e) {
            LOG.error("Exception in catch-all", e);
            doFail(e instanceof Exception ? (Exception) e : new RuntimeException("Caught error", e));

        } finally {
            processingThread = null;
        }
        return null;
    }

    public abstract void doRun();

    public abstract void doFail(Exception e);

    public void expired() {
        expired = true;
        exceeded = true;
        doFail(new WorkerRunnableException("Request has timed out"));
    }

    public void exceeded() {
        exceeded = true;
    }

    public void setTaskContext(WorkerRunnableContext taskContext) {
        this.taskContext = taskContext;
    }

    public WorkerRunnableContext getTaskContext() {
        return taskContext;
    }

    public String getSandboxId() {
        return taskContext.getSandboxIdentifier().getSandboxId();
    }

    public String getFullSandboxId() {
        return taskContext.getSandboxIdentifier().getFullSandboxId();
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isExceeded() {
        return exceeded;
    }

    public Thread getProcessingThread() {
        if (processingThread == null) return null;
        return processingThread.get();
    }

    public long getRequestTime() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    public long getProcessingTime() {
        //cant use ThreadsMBean in SubstrateVM
        return -1L;
    }

}
