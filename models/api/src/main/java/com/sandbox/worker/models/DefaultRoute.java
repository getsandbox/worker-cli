package com.sandbox.worker.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.models.interfaces.Route;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

public abstract class DefaultRoute implements Route {

    private String transport;
    private Map<String, String> properties;

    @ApiModelProperty(hidden = true)
    private RouteDefinitionSource definitionSource;

    private RouteConfig routeConfig;

    @Override
    public String getRouteIdentifier() {
        return RouteSupport.generateRouteIdentifier(getTransport(), getProperties());
    }

    @Override
    public String getTransport() {
        return transport;
    }

    @Override
    public void setTransport(String transport) {
        this.transport = transport;
    }

    @Override
    public Map<String, String> getProperties() {
        if(properties == null) properties = new HashMap<>();
        return properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public RouteDefinitionSource getDefinitionSource() {
        return definitionSource;
    }

    @Override
    public void setDefinitionSource(RouteDefinitionSource definitionSource) {
        this.definitionSource = definitionSource;
    }

    @Override
    public RouteConfig getRouteConfig() {
        return routeConfig;
    }

    @Override
    public void setRouteConfig(RouteConfig routeConfig) {
        this.routeConfig = routeConfig;
    }
}
