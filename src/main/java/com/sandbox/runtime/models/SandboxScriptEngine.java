package com.sandbox.runtime.models;

import com.sandbox.runtime.js.models.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Created by nickhoughton on 18/09/2014.
 */
public class SandboxScriptEngine {

    ScriptEngine engine;

    ScriptContext context;

    Console console;

    static Logger logger = LoggerFactory.getLogger(SandboxScriptEngine.class);

    public SandboxScriptEngine() {
    }

    public SandboxScriptEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public ScriptContext getContext() {
        return context;
    }

    public void setContext(ScriptContext context) {
        this.context = context;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

}
