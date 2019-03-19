package com.sandbox.runtime.js.services;

import com.sandbox.runtime.converters.NashornConverter;
import com.sandbox.runtime.js.models.SandboxScriptEngine;
import com.sandbox.runtime.models.MetadataService;
import com.sandbox.runtime.models.RuntimeVersion;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

/**
 * Created by nickhoughton on 3/01/2016.
 */
public class ServiceManager {

    @Autowired
    ApplicationContext context;

    @Qualifier("metadataService")
    @Autowired
    MetadataService metadataService;

    //the number of executions between 'refreshes' of the engine context, refresh is expensive and unnecessary for every call.
    private int refreshThreshold = 0;
    private Map<String, AtomicInteger> counters = new HashMap<>();
    private Map<String, Service> services = new ConcurrentHashMap<>();
    private Map<String, String> fullSandboxReference = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> configs = new ConcurrentHashMap<>();
    private Map<RuntimeVersion, JSEngineService> engineServices = new ConcurrentHashMap<>();
    private ExecutorService executorService = null;

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    public ServiceManager() {
    }

    public ServiceManager(int refreshThreshold) {
        this.refreshThreshold = refreshThreshold;
        if(refreshThreshold > 0){
            executorService = Executors.newFixedThreadPool(1);
        }
    }

    @PostConstruct
    public void init(){
        engineServices.put(RuntimeVersion.getLatest(), context.getBean(JSEngineService.class, RuntimeVersion.getLatest()));
    }

    public void warmup(){
        boolean engineQueueWorking = true;
        //wait for downstream engine services to become ready
        while(engineQueueWorking){
            engineQueueWorking = false;
            for (JSEngineService engineService : engineServices.values()){
                if(!engineService.isReady()){
                    engineQueueWorking = true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("Warm-up interrupted", e);
            }
        }
    }

    public Service getValidationService(String fullSandboxId, String sandboxId){
        SandboxScriptEngine engine = getEngineServiceForSandbox(sandboxId).createOrGetEngine();
        Service result = (Service)context.getBean("droneValidationService", engine, fullSandboxId, sandboxId);
        addConfigToService(result);
        return result;
    }

    public Service getService(String fullSandboxId, String sandboxId){
        AtomicInteger counter = counters.getOrDefault(sandboxId, new AtomicInteger(0));
        counters.putIfAbsent(sandboxId, counter);
        counter.incrementAndGet();
        //if we have to refresh
        if(refreshThreshold > 0 && counter.get() % refreshThreshold == 0) {
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
        SandboxScriptEngine engine = getEngineServiceForSandbox(sandboxId).createOrGetEngine();
        Service service = (Service)context.getBean("droneService", engine, fullSandboxId, sandboxId);
        addConfigToService(service);
        fullSandboxReference.put(sandboxId, fullSandboxId);
        services.put(sandboxId, service);
        return service;
    }

    public void refreshService(String sandboxId){
        String fullSandboxId = fullSandboxReference.get(sandboxId);
        if(fullSandboxId == null){
            logger.warn("Call refreshService() before calling create()/get() for sandboxId: {}", sandboxId);
            return;
        }

        //asked for a refresh, so get new config, otherwise it uses local metadataService.
        configs.remove(sandboxId);
        //generate a new one with new changes
        createService(fullSandboxId, sandboxId);
        //find and refresh any sandboxes that are clones. if ids are equal, it is a change to a forked sb
        if(fullSandboxId.equals(sandboxId)){
            fullSandboxReference.entrySet().stream()
                    .filter( e -> { return e.getValue().equals(fullSandboxId); })
                    .forEach(e -> { createService(fullSandboxId, e.getKey()); });
        }
    }

    public void refreshAllServices(){
        configs.clear();
        fullSandboxReference.entrySet().stream()
                .forEach(e -> { createService(e.getValue(), e.getKey()); });
    }

    public void removeService(String sandboxId){
        services.remove(sandboxId);
        configs.remove(sandboxId);
        counters.remove(sandboxId);
        fullSandboxReference.remove(sandboxId);
    }

    private JSEngineService getEngineServiceForSandbox(String sandboxId){
        Map<String, String> config = getConfig(sandboxId);
        RuntimeVersion runtimeVersion = null;
        //get runtime version from sandbox config, might not be there, should be defaulted
        String configRuntimeVersion = config.get("sandbox_runtime_version");
        if(configRuntimeVersion != null){
            try {
                //get enum from value, throws IllegalArg if no matching enum is found
                runtimeVersion = RuntimeVersion.valueOf(configRuntimeVersion);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid runtime version number", e);
            }
        }
        //if we still dont have a runtime version, default to the latest
        if(runtimeVersion == null){
            runtimeVersion = RuntimeVersion.getLatest();
        }

        //use runtime version to get/create a engine service to give us our engine
        JSEngineService result = engineServices.get(runtimeVersion);
        if(result == null){
            //no matching engine service, will have to create one!
            result = context.getBean(JSEngineService.class, runtimeVersion);
            engineServices.put(runtimeVersion, result);
        }
        return result;
    }

    private Map<String, String> getConfig(String sandboxId){
        Map<String, String> config = configs.get(sandboxId);
        if(config == null){
            config = metadataService.getConfigForSandboxId(sandboxId);
            configs.put(sandboxId, config);
        }
        return config;
    }

    private Service addConfigToService(Service result){
        Map<String, String> config = getConfig(result.sandboxId);
        try {
            result.scriptObject.setConfig((ScriptObject) NashornConverter.instance().convert(result.getSandboxScriptEngine().getEngine(), config));
        } catch (Exception e) {
            logger.error("Error converting config", e);
        }
        return result;
    }
}
