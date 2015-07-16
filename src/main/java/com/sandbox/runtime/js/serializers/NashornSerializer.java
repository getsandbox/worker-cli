package com.sandbox.runtime.js.serializers;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Created by nickhoughton on 16/07/2015.
 */
public class NashornSerializer {

    private static final Logger logger = LoggerFactory.getLogger(NashornSerializer.class);

    private ScriptObjectMirror jsonParser;

    public NashornSerializer(ScriptObjectMirror jsonParser) {
        this.jsonParser = jsonParser;
    }

    private static NashornSerializer instance = null;
    public static NashornSerializer instance(){
        if(instance == null){
            try {
                ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine(new String[]{"--no-java"});
                instance = new NashornSerializer((ScriptObjectMirror) engine.eval("JSON"));
            } catch (ScriptException e) {
                logger.error("Error starting nashorn serialiser!", e);
            }
        }
        return instance;
    }

    public String serialize(Object value){
        Object result = jsonParser.callMember("stringify", value, null, 2);
        if(!(result instanceof String)){
            result = "null";
            logger.warn("Serialized unsupported object, returning null..");
        }
        return (String) result;
    }

}
