package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.ServiceScriptException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickhoughton on 25/09/2014.
 */
public class ServiceBox implements ISandboxScriptObject{
    private List<RouteDetails> routes = new ArrayList<RouteDetails>();

    /**
     * Don't pass functions between Nashorn and Java.
     * Pass objects which implement functional interfaces.
     * OR
     * Anonymous functions mapping to interfaces
     */
    public void define(String path, String method, ISandboxDefineCallback func) throws ServiceScriptException {
        RouteDetails routeDetails = new RouteDetails(method, path);
        routes.add(routeDetails);
    }

    public void define(String path, String method, Object func) throws ServiceScriptException {
        throw new ServiceScriptException("Invalid callback definition for route: " + method.toUpperCase() + " " + path + " is: " + func);
    }

    /**
     * for service defns omitting optional HTTP method type
     */
    public void define(String _path, ISandboxDefineCallback func) throws ServiceScriptException {
        this.define(_path, "get", func);
    }

    public void define(String _path, Object func) throws ServiceScriptException {
        this.define(_path, "get", func);
    }

    // TODO: deep clone
    public List<RouteDetails> getRoutes() { return routes; }

    public void seedState(String func) { }
}
