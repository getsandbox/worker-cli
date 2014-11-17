package com.sandbox.runtime.models;

import jdk.nashorn.internal.objects.NativeError;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.RecompilableScriptFunctionData;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptFunctionData;
import jdk.nashorn.internal.runtime.Source;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by nickhoughton on 15/11/2014.
 */
public class ScriptSource implements Serializable {

    private static final long serialVersionUID = 5416349252348357175L;
    String path;
    int lineNumber;
    String implementation;

    private static Field scriptSourceField = null;
    private static Field scriptSourceSourceField = null;
    private static Field scriptSourceLineField = null;

    static {
        try {
            scriptSourceField = ScriptFunction.class.getDeclaredField("data");
            scriptSourceField.setAccessible(true);

            scriptSourceSourceField = RecompilableScriptFunctionData.class.getDeclaredField("source");
            scriptSourceSourceField.setAccessible(true);

            scriptSourceLineField = RecompilableScriptFunctionData.class.getDeclaredField("lineNumber");
            scriptSourceLineField.setAccessible(true);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public ScriptSource() {
    }

    public ScriptSource(String path, int lineNumber) {
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public ScriptSource(ScriptFunction function) {
        if(scriptSourceField != null){
            try {
                ScriptFunctionData sourceData = (ScriptFunctionData) scriptSourceField.get(function);
                path = ((Source)scriptSourceSourceField.get(sourceData)).getName();
                lineNumber = (int) scriptSourceLineField.get(sourceData);
                implementation = function.toSource();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public ScriptSource(NativeError error, String elementIdentifier){

        ECMAException exception = (ECMAException) error.nashornException;
        StackTraceElement[] stack = exception.getStackTrace();
        for(int x=0; x < stack.length; x++){
            StackTraceElement element = stack[x];
            StackTraceElement nextElement = (stack.length-1 >= x+1) ? stack[x+1] : null;
            if(element != null && element.getFileName().contains(elementIdentifier)){
                path = nextElement.getFileName();
                lineNumber = nextElement.getLineNumber();
                break;
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }
}
