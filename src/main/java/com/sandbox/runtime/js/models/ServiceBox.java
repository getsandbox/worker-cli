package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.ScriptSource;
import com.sandbox.common.models.ServiceScriptException;
import com.sandbox.runtime.models.http.HTTPRouteDetails;
import com.sandbox.runtime.models.jms.JMSRouteDetails;
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

    public void define(String transport, String defineType, String path, String method, ScriptObject properties, ScriptFunction callback, ISandboxDefineCallback func, NativeError error) throws ServiceScriptException {
        Map<String, String> propertiesMap = new HashMap<>();
        properties.propertyIterator().forEachRemaining(k -> {
            Object value = properties.get(k);
            if(value instanceof String) propertiesMap.put(k, (String) value);
            return;
        });

        RouteDetails routeDetails = null;
        if(transport.equals("http")){
            routeDetails = new HTTPRouteDetails(method, path, propertiesMap);

        }else if(transport.equals("jms")){
            routeDetails = new JMSRouteDetails(path, propertiesMap);

        }
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
