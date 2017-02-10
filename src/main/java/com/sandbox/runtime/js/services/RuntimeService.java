package com.sandbox.runtime.js.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sandbox.runtime.models.config.RuntimeConfig;
import com.sandbox.runtime.converters.NashornConverter;
import com.sandbox.runtime.js.models.SandboxScriptEngine;
import com.sandbox.runtime.utils.JSONUtils;
import com.sandbox.runtime.js.utils.NashornUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.script.ScriptContext;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by drew on 2/08/2014.
 */
public class RuntimeService extends Service {

    @Autowired
    RuntimeConfig config;

    //state is persisted across requests, but not stored.
    static Object convertedState = null;

    public RuntimeService(SandboxScriptEngine sandboxScriptEngine, NashornUtils nashornUtils, String fullSandboxId, String sandboxId) {
        super(sandboxScriptEngine, nashornUtils, fullSandboxId, sandboxId);
    }

    @PostConstruct
    public void init(){
        if(config.getStatePath() != null){
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    cache.setSandboxState(sandboxId, mapper.writeValueAsString(convertedState));
                } catch (JsonProcessingException e) {
                    logger.error("Error serialising state", e);
                }
            }, 30, 30, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                //save state before shutdown
                logger.info("Persisting state before shutdown..");
                try {
                    cache.setSandboxState(sandboxId, mapper.writeValueAsString(convertedState));
                } catch (JsonProcessingException e) {
                    logger.error("Error serialising state", e);
                }
            }));
        }
    }

    @Override
    protected void setState() throws Exception {

        if(convertedState == null){
            String currentState = cache.getSandboxState(sandboxId);
            convertedState = NashornConverter.instance().convert(sandboxScriptEngine.getEngine(), JSONUtils.parse(mapper, currentState));
        }

        sandboxScriptEngine.getContext().setAttribute(
                "state",
                convertedState,
                ScriptContext.ENGINE_SCOPE
        );

    }

    @Override
    protected void saveState(Object state) throws Exception {
        convertedState = state;
    }

}
