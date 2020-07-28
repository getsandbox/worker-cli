package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.models.WorkerRunnable;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.RuntimeResponse;
import com.sandbox.worker.models.interfaces.HTTPRoute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class WorkerRequestRunnable extends WorkerRunnable {

    private HttpRuntimeRequest runtimeRequest;
    private HTTPRoute routeMatch;
    private BiConsumer<HttpRuntimeResponse, String> nextFunction;
    private BiConsumer<Throwable, String> failureResponseFunction;

    private ProcessRequestExecutor processRequestExecutor;

    private ExecutorService ioExecutor;
    private ScheduledExecutorService delayedResponder;
    private ObjectMapper mapper;

    public WorkerRequestRunnable(HttpRuntimeRequest runtimeRequest, HTTPRoute routeMatch, BiConsumer<HttpRuntimeResponse, String> nextFunction, BiConsumer<Throwable, String> failureResponseFunction,
                                 ProcessRequestExecutor processRequestExecutor, ExecutorService ioExecutor, ScheduledExecutorService delayedResponder, ObjectMapper mapper) {
        this.runtimeRequest = runtimeRequest;
        this.routeMatch = routeMatch;
        this.nextFunction = nextFunction;
        this.failureResponseFunction = failureResponseFunction;
        this.processRequestExecutor = processRequestExecutor;
        this.ioExecutor = ioExecutor;
        this.delayedResponder = delayedResponder;
        this.mapper = mapper;
    }

    @Override
    public void doRun() {
        try {
            HttpRuntimeResponse runtimeResponse = processRequestExecutor.execute(runtimeRequest, getTaskContext().getSandboxScriptContext());
            String consoleOutput = getTaskContext().getSandboxScriptContext().getAndResetExecutionContextOutput();

            //add delays if configured or just fire function to complete
            runtimeResponse.setDurationMillis(System.currentTimeMillis() - runtimeRequest.getReceivedTimestamp());
            long calculatedDelayForRoute = calculateResponseDelay(runtimeResponse, routeMatch, runtimeRequest.getReceivedTimestamp());
            if (calculatedDelayForRoute > 0) {
                LOG.debug("Applying delay of {} ms", calculatedDelayForRoute);
                delayedResponder.schedule(() -> nextFunction.accept(runtimeResponse, consoleOutput), calculatedDelayForRoute, TimeUnit.MILLISECONDS);
            } else {
                ioExecutor.submit(() -> nextFunction.accept(runtimeResponse, consoleOutput));
            }

        } catch (Exception e) {
            doFail(e);
            return;
        }
    }

    @Override
    public void doFail(Exception e) {
        if(LOG.isDebugEnabled()) {
            LOG.error("Error processing request", e);
        } else {
            LOG.error("Error processing request - {}", e.getMessage());
        }

        String consoleOutput = null;
        try {
            consoleOutput = getTaskContext().getSandboxScriptContext().getAndResetExecutionContextOutput();
        } catch (Exception e1) {
            LOG.warn("Error retrieving failure console output", e1);
        }
        failureResponseFunction.accept(e, consoleOutput);
    }

    private long calculateResponseDelay(RuntimeResponse runtimeResponse, HTTPRoute routeMatch, long startTime) {
        //now execution has finished, we can apply delays as configured
        long calculatedDelayForRoute = 0;
        //if we have a programmatically set delay, use that.
        if (runtimeResponse.getResponseDelay() > 0) {
            calculatedDelayForRoute = runtimeResponse.getResponseDelay();
        }

        //otherwise check route config
        if (calculatedDelayForRoute == 0) {
            calculatedDelayForRoute = RouteSupport.calculateDelay(routeMatch.getRouteConfig(), startTime, new AtomicInteger(1));
        }

        return calculatedDelayForRoute;
    }

    public HttpRuntimeRequest getRuntimeRequest() {
        return runtimeRequest;
    }
}
