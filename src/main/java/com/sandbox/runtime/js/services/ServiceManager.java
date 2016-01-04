package com.sandbox.runtime.js.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nickhoughton on 3/01/2016.
 */
public class ServiceManager {

    @Autowired
    ApplicationContext context;

    //the number of executions between 'refreshes' of the engine context, refresh is expensive and unnecessary for every call.
    private int refreshThreshold = 1;
    private Map<String, AtomicInteger> counters = new HashMap<>();
    private Map<String, Service> services = new ConcurrentHashMap<>();

    public ServiceManager() {
    }

    public ServiceManager(int refreshThreshold) {
        this.refreshThreshold = refreshThreshold;
    }

    public Service getValidationService(String fullSandboxId, String sandboxId){
        return (Service)context.getBean("droneValidationService", fullSandboxId, sandboxId);
    }

    public Service getService(String fullSandboxId, String sandboxId){
        AtomicInteger counter = counters.getOrDefault(sandboxId, new AtomicInteger(0));
        counter.incrementAndGet();
        //if we don't have to refresh the service, and we have an existing service then return it
        if(counter.get() % refreshThreshold != 0 && services.containsKey(sandboxId)) return services.get(sandboxId);

        counters.putIfAbsent(sandboxId, counter);
        return createService(fullSandboxId, sandboxId);
    }

    private Service createService(String fullSandboxId, String sandboxId){
        Service service = (Service)context.getBean("droneService", fullSandboxId, sandboxId);
        services.put(sandboxId, service);
        return service;
    }

    public void refreshService(String fullSandboxId, String sandboxId){
        //generate a new one with new changes
        createService(fullSandboxId, sandboxId);
    }

    public void removeService(String sandboxId){
        services.remove(sandboxId);
        counters.remove(sandboxId);
    }
}
