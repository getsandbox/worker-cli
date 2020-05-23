package com.sandbox.worker.models;

public class RouteDefinitionSource {

    private ScriptSource defineSource;
    private String defineType;
    private ScriptSource functionSource;

    public RouteDefinitionSource() {
    }

    public RouteDefinitionSource(String defineType, ScriptSource defineSource, ScriptSource functionSource) {
        this.defineSource = defineSource;
        this.defineType = defineType;
        this.functionSource = functionSource;
    }

    public ScriptSource getDefineSource() {
        return defineSource;
    }

    public void setDefineSource(ScriptSource defineSource) {
        this.defineSource = defineSource;
    }

    public String getDefineType() {
        return defineType;
    }

    public void setDefineType(String defineType) {
        this.defineType = defineType;
    }

    public ScriptSource getFunctionSource() {
        return functionSource;
    }

    public void setFunctionSource(ScriptSource functionSource) {
        this.functionSource = functionSource;
    }
}
