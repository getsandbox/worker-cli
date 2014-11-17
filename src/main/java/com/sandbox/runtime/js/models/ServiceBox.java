package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.ScriptSource;
import com.sandbox.runtime.models.ServiceScriptException;
import jdk.nashorn.internal.objects.NativeError;
import jdk.nashorn.internal.runtime.ScriptFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickhoughton on 25/09/2014.
 */
public class ServiceBox implements ISandboxScriptObject{
    private List<RouteDetails> routes = new ArrayList<RouteDetails>();

    public void define(String path, String method, ScriptFunction callback, NativeError error, ISandboxDefineCallback func) throws ServiceScriptException {
        RouteDetails routeDetails = new RouteDetails(method, path);
        routeDetails.setFunctionSource(new ScriptSource(callback));
        routeDetails.setDefineSource(new ScriptSource(error, "<sandbox-internal>"));
        routes.add(routeDetails);
    }

    public List<RouteDetails> getRoutes() { return routes; }

    public void seedState(String func) { }
}
