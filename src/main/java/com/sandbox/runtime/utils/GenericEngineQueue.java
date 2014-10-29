package com.sandbox.runtime.utils;

import com.sandbox.runtime.models.SandboxScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.script.ScriptEngine;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by nickhoughton on 18/10/2014.
 */
public abstract class GenericEngineQueue {
    static Logger logger = LoggerFactory.getLogger(GenericEngineQueue.class);
    protected ApplicationContext context;
    ArrayBlockingQueue<SandboxScriptEngine> engines = new ArrayBlockingQueue<SandboxScriptEngine>(100);
    int targetInQueue;
    Thread filler;

    public GenericEngineQueue(ApplicationContext context, int targetInQueue) {
        this.context = context;
        this.targetInQueue = targetInQueue;
    }

    public int numberOfEngines(){
        return engines.size();
    }

    public void fill(){
        int added = 0;
        while(engines.size() < targetInQueue){
            ScriptEngine engine = context.getBean(ScriptEngine.class);
            SandboxScriptEngine sandboxScriptEngine = new SandboxScriptEngine(engine);

            initializeEngineIfNeeded(sandboxScriptEngine);
            prepareEngine(sandboxScriptEngine);

            engines.add(sandboxScriptEngine);

            added++;

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(added > 0) logger.debug("Filled engine queue: " + added + " engines created.");
    }

    public SandboxScriptEngine get(){
        SandboxScriptEngine engine = engines.poll();
        if(engine == null){
            //if failed, probably empty, so just get a fresh one.
            logger.warn("Had to create engine during processing, engine queue is empty?");
            ScriptEngine scriptEngine = context.getBean(ScriptEngine.class);
            engine = new SandboxScriptEngine(scriptEngine);

            initializeEngineIfNeeded(engine);
            prepareEngine(engine);
        }

        return postProcessEngine(engine);
    }

    public void put(SandboxScriptEngine sandboxEngine){
        prepareEngine(sandboxEngine);
        engines.add(sandboxEngine);
    }

    //only initialize each ScriptEngine obj once
    Set<Integer> initialized = new HashSet<>();
    protected SandboxScriptEngine initializeEngineIfNeeded(SandboxScriptEngine sandboxEngine){
        if(!initialized.contains(sandboxEngine.getEngine().hashCode())){
            initializeEngine(sandboxEngine);
            initialized.add(sandboxEngine.getEngine().hashCode());
        }

        return sandboxEngine;
    }

    //executed once when the engine is created, load in a seal all the libs
    protected abstract SandboxScriptEngine initializeEngine(SandboxScriptEngine sandboxEngine);

    //this is the executed per request, so everytime the engine goes back into the queue this runs to clear any junk the user might have left
    protected abstract SandboxScriptEngine prepareEngine(SandboxScriptEngine sandboxEngine);

    protected abstract SandboxScriptEngine postProcessEngine(SandboxScriptEngine sandboxEngine);

    public void stop() {
        filler.interrupt();
    }

    public boolean isRunning() {
        return filler.isAlive();
    }

    public void start(){
        if(filler != null) throw new RuntimeException("Already started");

        filler = new Thread(() -> {
            fill();

        });

        filler.start();
    }

}
