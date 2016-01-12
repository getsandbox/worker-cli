package com.sandbox.runtime.js.services;

import com.sandbox.runtime.js.models.Console;
import com.sandbox.runtime.js.utils.FileUtils;
import com.sandbox.runtime.js.utils.NashornRuntimeUtils;
import com.sandbox.runtime.models.SandboxScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

/**
 * Created by nickhoughton on 3/12/2015.
 */
public class JSEngineService {

    static Logger logger = LoggerFactory.getLogger(JSEngineService.class);
    private ScriptEngine engine;

    @Autowired
    ApplicationContext context;

    public JSEngineService() {
    }

    @PostConstruct
    public void start(){
        this.engine = context.getBean(ScriptEngine.class);
        createEngine();
    }

    public SandboxScriptEngine createEngine(){
        logger.debug("Creating new engine..");
        SandboxScriptEngine sandboxEngine = new SandboxScriptEngine(engine);

        //create scopes, context and setup bits and pieces
        createNewContext(sandboxEngine);

        //inject 3rd part libs, lodash etc.
        injectLibraries(sandboxEngine);

        //monkey patch, move things into place, remove things etc.
        patchEngine(sandboxEngine);

        return sandboxEngine;
    }

    private SandboxScriptEngine createNewContext(SandboxScriptEngine sandboxEngine) {
        NashornRuntimeUtils nashornRuntimeUtils = (NashornRuntimeUtils) context.getBean("nashornUtils", "temporary");

        Console consoleInstance = context.getBean(Console.class);
        sandboxEngine.setConsole(consoleInstance);

        Bindings globalScope = sandboxEngine.getEngine().getContext().getBindings(ScriptContext.GLOBAL_SCOPE);
        if (globalScope == null) {
            engine.getContext().setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
            globalScope = engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE);
        }

        final Bindings engineScope = new SimpleBindings();
        final ScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
        ctx.setBindings(engineScope, ScriptContext.ENGINE_SCOPE);
        ctx.setAttribute("_console", sandboxEngine.getConsole(), ScriptContext.ENGINE_SCOPE);
        ctx.setAttribute("nashornUtils", nashornRuntimeUtils, ScriptContext.ENGINE_SCOPE);
        sandboxEngine.setContext(ctx);

        return sandboxEngine;
    }

    private SandboxScriptEngine patchEngine(SandboxScriptEngine sandboxEngine){
        final Bindings engineScope = sandboxEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        // monkey patch nashorn
        try {
            engineScope.put(ScriptEngine.FILENAME, "<sandbox-internal>");
            sandboxEngine.getEngine().eval(FileUtils.loadJSFromResource("sandbox-patch"), sandboxEngine.getContext());
        } catch (ScriptException e) {
            logger.error("Error postProcessing engine",e);
        }

        return sandboxEngine;
    }

    private SandboxScriptEngine injectLibraries(SandboxScriptEngine sandboxEngine){
        final Bindings globalScope = sandboxEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE);
        final Bindings engineScope = sandboxEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

        try {
            loadAndSealScript("lodash-2.4.1.js","lib/lodash-2.4.1.min", "_", engineScope, sandboxEngine.getEngine());
            loadAndSealScript("faker.js","lib/faker-2.1.2.min", "faker", globalScope, sandboxEngine.getEngine());
            loadAndSealScript("moment.js", "lib/moment-2.8.2.min", "moment", globalScope, sandboxEngine.getEngine());
            loadAndSealScript("amanda.js", "lib/amanda-0.4.8.min", "amanda", globalScope, sandboxEngine.getEngine());
            loadAndSealScript("validator.js", "lib/validator.min", "validator", globalScope, sandboxEngine.getEngine());
            loadAndSealScript("sandbox-validator.js", "sandbox-validator", "sandboxValidator", globalScope, sandboxEngine.getEngine());

        } catch (ScriptException e) {
            logger.error("Error loading 3rd party JS", e);
        }

        return sandboxEngine;
    }

    private void loadAndSealScript(String name, String file, String objectName, Bindings scope, ScriptEngine engine) throws ScriptException {
        scope.put(ScriptEngine.FILENAME, name);
        engine.eval(FileUtils.loadJSFromResource(file), scope);
        engine.eval("Object.freeze(" + objectName + "); Object.seal(" + objectName + ");", scope);
        scope.put(objectName, engine.eval(objectName, scope));
    }

}
