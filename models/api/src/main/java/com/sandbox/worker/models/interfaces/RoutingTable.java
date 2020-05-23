package com.sandbox.worker.models.interfaces;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sandbox.worker.models.DefaultRoutingTable;
import com.sandbox.worker.models.RuntimeRequest;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        defaultImpl = DefaultRoutingTable.class,
        visible = true
)
public interface RoutingTable {
    String getRepositoryId();

    void setRepositoryId(String repositoryId);

    List<Route> getRouteDetails();

    void setRouteDetails(List<Route> routeDetails);

    void addRouteDetails(Route routeDetails);

    HTTPRoute findMatch(String requestMethod, String requestPath, Map<String, String> properties);

    Route findMatch(String requestTransport, String requestMethod, String requestPath, Map<String, String> properties);

    Route findMatch(RuntimeRequest runtimeRequest);

}
