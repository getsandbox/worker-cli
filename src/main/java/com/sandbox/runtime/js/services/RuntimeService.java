package com.sandbox.runtime.js.services;

import com.sandbox.runtime.js.converters.NashornConverter;
import com.sandbox.runtime.js.models.JsonNode;
import com.sandbox.runtime.models.SandboxScriptEngine;

import javax.script.ScriptContext;


/**
 * Created by drew on 2/08/2014.
 */
public class RuntimeService extends Service {

    public RuntimeService(SandboxScriptEngine sandboxScriptEngine) {
        super(sandboxScriptEngine);
    }

    //state is persisted across requests, but not stored.
    static Object convertedState = null;

    @Override
    protected void setState() throws Exception {

        if(convertedState == null){
            String currentState = cache.getSandboxState(sandboxId);
            JsonNode state = new JsonNode(currentState);
            convertedState = NashornConverter.instance().convert(sandboxScriptEngine.getEngine(), state.getJsonObject());
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
