package com.sandbox.runtime.js.services;

import com.sandbox.runtime.js.converters.NashornConverter;
import com.sandbox.runtime.models.Cache;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nickhoughton on 3/01/2016.
 */
public class ServiceManager {

    @Autowired
    ApplicationContext context;

    @Autowired
    Cache cache;

    //the number of executions between 'refreshes' of the engine context, refresh is expensive and unnecessary for every call.
    private int refreshThreshold = 1;
    private Map<String, AtomicInteger> counters = new HashMap<>();
    private Map<String, Service> services = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> sandboxConfig = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    public ServiceManager() {
    }

    public ServiceManager(int refreshThreshold) {
        this.refreshThreshold = refreshThreshold;
    }

    public Service getValidationService(String fullSandboxId, String sandboxId){
        Service result = (Service)context.getBean("droneValidationService", fullSandboxId, sandboxId);
        addConfigToService(result);
        return result;
    }

    public Service getService(String fullSandboxId, String sandboxId){
        AtomicInteger counter = counters.getOrDefault(sandboxId, new AtomicInteger(0));
        counters.putIfAbsent(sandboxId, counter);
        counter.incrementAndGet();
        //if we have to refresh
        if(counter.get() % refreshThreshold == 0) {
            executorService.execute(() -> {
                createService(fullSandboxId, sandboxId);
            });
        }
        //we have an existing service then return it
        if(services.containsKey(sandboxId)) {
            return services.get(sandboxId);
        }

        return createService(fullSandboxId, sandboxId);
    }

    private Service createService(String fullSandboxId, String sandboxId){
        Service service = (Service)context.getBean("droneService", fullSandboxId, sandboxId);
        addConfigToService(service);
        services.put(sandboxId, service);
        return service;
    }

    public void refreshService(String fullSandboxId, String sandboxId){
        //asked for a refresh, so get new config, otherwise it uses local cache.
        sandboxConfig.remove(sandboxId);
        //generate a new one with new changes
        createService(fullSandboxId, sandboxId);
    }

    public void removeService(String sandboxId){
        services.remove(sandboxId);
        sandboxConfig.remove(sandboxId);
        counters.remove(sandboxId);
    }

    private Service addConfigToService(Service result){
        Map<String, String> config = sandboxConfig.get(result.sandboxId);
        if(config == null){
            config = cache.getConfigForSandboxId(result.sandboxId);
            sandboxConfig.put(result.sandboxId, config);
        }
        try {
            result.scriptObject.setConfig((ScriptObject) NashornConverter.instance().convert(result.getSandboxScriptEngine().getEngine(), config));
        } catch (Exception e) {
            logger.error("Error converting config", e);
        }
        return result;
    }
}
