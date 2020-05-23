package com.sandbox.worker.core.js;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.utils.ErrorUtils;

import org.graalvm.polyglot.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJSExecutor<R, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJSExecutor.class);

    public T execute(R input, WorkerScriptContext scriptContext) throws ServiceScriptException {
        return doExecute(input, scriptContext);
    }

    protected abstract T doExecute(R input, WorkerScriptContext executionContext) throws ServiceScriptException;

    public void bootstrap(WorkerScriptContext scriptContext) throws ServiceScriptException {
        //skip bootstrap if its not needed
        if(!scriptContext.needsBootstrap()){
            return;
        }

        ScriptFunctions scriptFunctions = scriptContext.getScriptFunctions();

        //create and add Sandbox JS object to context to and expose handle define() calls
        //take context and bootstrap context to trigger `Sandbox.define(..)` then get routes
        try {
            if (scriptFunctions.hasFile("main.mjs")){
                LOG.debug("Bootstrapping with found 'main.mjs'");
                Source mainModule = Source.newBuilder("js", scriptFunctions.readFile( "main.mjs"), "main.mjs").build();
                JSContextHelper.eval(scriptContext, mainModule);
            } else {
                LOG.debug("Bootstrapping with found 'main.js'");
                Source mainModule = Source.newBuilder("js", scriptFunctions.readFile( "main.js"), "main.js").build();
                JSContextHelper.eval(scriptContext, mainModule);
            }
            scriptContext.setNeedsBootstrap(false);

        } catch (Exception e) {
            throw ErrorUtils.getServiceScriptException(e);
        }
    }

}
