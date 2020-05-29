package com.sandbox.worker.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.models.enums.RuntimeTransportType;
import com.sandbox.worker.models.interfaces.HTTPRoute;

import java.lang.ref.SoftReference;
import java.util.Map;
import org.apache.cxf.custom.jaxrs.model.ExactMatchURITemplate;

public class DefaultHTTPRoute extends DefaultRoute implements HTTPRoute {

    private String method;
    private String path;
    @JsonIgnore
    private SoftReference<ExactMatchURITemplate> pathTemplate;

    public DefaultHTTPRoute() {
        super();
        setTransport(RuntimeTransportType.HTTP.name().toLowerCase());
    }

    public DefaultHTTPRoute(String method, String path, Map<String, String> properties) {
        this();

        //coalesce varied wildcard method into one
        this.method = RouteSupport.sanitiseRouteMethod(method);
        this.path = RouteSupport.sanitiseRoutePath(path);
        setProperties(properties);
    }

    @Override
    public String getRouteIdentifier() {
        return RouteSupport.generateRouteIdentifier(getTransport(), getMethod(), getPath(), getProperties());
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public ExactMatchURITemplate getPathTemplate() {
        ExactMatchURITemplate result;
        if(pathTemplate == null || (result = pathTemplate.get()) == null) {
            result = RouteSupport.getTemplate(this.path);
            pathTemplate = new SoftReference<>(result);
        }
        return result;
    }

    @JsonIgnore
    @Override
    public String getProcessingKey() {
        return getPathTemplate().getLiteralChars();
    }

    @JsonIgnore
    @Override
    public String getDisplayKey() {
        return getPath().concat(getMethod());
    }

}
