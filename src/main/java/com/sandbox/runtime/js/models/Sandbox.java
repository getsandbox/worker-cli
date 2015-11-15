package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.common.models.ServiceScriptException;
import jdk.nashorn.internal.objects.NativeError;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;

/**
 * Created by drew on 30/07/2014.
 */
public class Sandbox extends ServiceBox {

    EngineRequest req;
    EngineResponse res;
    private boolean matched;
    private ISandboxDefineCallback matchedFunction;

    public boolean isMatched() {
        return matched;
    }

    public Sandbox(EngineRequest req, EngineResponse res) {
        this.req = req;
        this.res = res;
    }

    /**
     * Don't pass functions between Nashorn and Java.
     * Pass objects which implement functional interfaces.
     * OR
     * Anonymous functions mapping to interfaces
     */
    @Override
    public void define(String transport, String defineType, String path, String method, ScriptObject properties, ScriptFunction callback, ISandboxDefineCallback func, NativeError error) throws ServiceScriptException {
        super.define(transport, defineType, path, method, properties, callback, func, error);

        //get routeDetails just created by super.define()
        RouteDetails routeDetails = super.currentRoute;

        if (routeDetails.matchesEngineRequest(req)){
            // flag match was found
            this.matched = true;
            // store the callback for when the whole file has been processed
            matchedFunction = (ISandboxDefineCallback) func;
        }
    }

    public ISandboxDefineCallback getMatchedFunction() {
        return matchedFunction;
    }
}