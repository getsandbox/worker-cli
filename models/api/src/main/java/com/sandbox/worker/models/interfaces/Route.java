package com.sandbox.worker.models.interfaces;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sandbox.worker.models.DefaultHTTPRoute;
import com.sandbox.worker.models.RouteConfig;
import com.sandbox.worker.models.RouteDefinitionSource;

import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "transport",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultHTTPRoute.class, name = "http"),
})
public interface Route {

    default void setRouteIdentifier(String routeIdentifier) {
        //noop its generated this is just jackson
    }

    String getRouteIdentifier();

    String getTransport();

    void setTransport(String transport);

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    String getProcessingKey();

    String getDisplayKey();

    RouteDefinitionSource getDefinitionSource();

    void setDefinitionSource(RouteDefinitionSource definitionSource);

    RouteConfig getRouteConfig();

    void setRouteConfig(RouteConfig routeConfig);

}
