package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.ScriptSource;
import com.sandbox.runtime.models.ServiceScriptException;
import jdk.nashorn.internal.objects.NativeError;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickhoughton on 25/09/2014.
 */
public class ServiceBox implements ISandboxScriptObject{
    private List<RouteDetails> routes = new ArrayList<RouteDetails>();

    RouteDetails currentRoute;

    public void define(String transport, String defineType, String path, String method, ScriptObject headers, ScriptFunction callback, ISandboxDefineCallback func, NativeError error) throws ServiceScriptException {
        Map<String, String> headersMap = new HashMap<>();
        headers.propertyIterator().forEachRemaining(k -> {
            Object value = headers.get(k);
            if(value instanceof String) headersMap.put(k, (String) value);
            return;
        });

        RouteDetails routeDetails = new RouteDetails(method, path, headersMap);
        routeDetails.setTransport(transport);
        routeDetails.setFunctionSource(new ScriptSource(callback));
        routeDetails.setDefineSource(new ScriptSource(error, "<sandbox-internal>"));
        routeDetails.setDefineType(defineType);

        //set property for extension classes
        currentRoute = routeDetails;

        routes.add(routeDetails);
    }

    public List<RouteDetails> getRoutes() { return routes; }

    public void seedState(String func) { }
}
