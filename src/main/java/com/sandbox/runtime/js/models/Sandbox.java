package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.HTTPRequest;
import com.sandbox.runtime.models.HTTPResponse;
import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.ServiceScriptException;

/**
 * Created by drew on 30/07/2014.
 */
public class Sandbox extends ServiceBox {

    HTTPRequest req;
    HTTPResponse res;
    private boolean matched;
    private ISandboxDefineCallback matchedFunction;

    public boolean isMatched() {
        return matched;
    }

    public Sandbox(HTTPRequest req, HTTPResponse res) {
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
    public void define(String path, String method, ISandboxDefineCallback func) throws ServiceScriptException {
        super.define(path, method, func);
        RouteDetails routeDetails = new RouteDetails(method, path);

        if (req.getPath().equals(routeDetails.getPath()) && routeDetails.matchesMethod(req.getMethod()) ) {
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