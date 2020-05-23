package com.sandbox.worker.models;

import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.models.interfaces.HTTPRoute;
import com.sandbox.worker.models.interfaces.Route;
import com.sandbox.worker.models.interfaces.RoutingTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultRoutingTable implements Serializable, RoutingTable {

    private static final long serialVersionUID = 5416349625258357175L;
    String repositoryId;
    List<Route> routeDetails;
    private boolean routesSorted = false;

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }

    @Override
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Override
    public List<Route> getRouteDetails() {
        return routeDetails;
    }

    @Override
    public void setRouteDetails(List<Route> routeDetails) {
        this.routeDetails = routeDetails;
    }

    @Override
    public void addRouteDetails(Route routeDetails){
        if(getRouteDetails() == null){
            setRouteDetails(new ArrayList<>());
        }

        getRouteDetails().add(routeDetails);

    }

    @Override
    public HTTPRoute findMatch(String requestMethod, String requestPath, Map<String, String> properties) {
        return (HTTPRoute) findMatch("HTTP", requestMethod, requestPath, properties);
    }

    @Override
    public Route findMatch(String requestTransport, String requestMethod, String requestPath, Map<String, String> properties) {
        if("HTTP".equalsIgnoreCase(requestTransport)) {
            HttpRuntimeRequest request = new HttpRuntimeRequest();
            request.setMethod(requestMethod);
            request.setUrl(requestPath);
            request.setProperties(properties);
            return findMatch(request);
        }else{
            return null;
        }
    }

    @Override
    public Route findMatch(RuntimeRequest runtimeRequest){
        List<Route> routes = getRouteDetails();
        if(routes == null) return null;

        //sort, put the longest route literals at the top, should theoretically be the best matches?!
        synchronized (this){
            if(!routesSorted){
                routes.sort((r1, r2) -> {
                    return r2.getProcessingKey().compareTo(r1.getProcessingKey());
                });
                routesSorted = true;
            }
        }

        for(Route route : routes){
            boolean isMatch = RouteSupport.isMatch(route, runtimeRequest);
            if(isMatch) return route;
        }

        return null;
    }
}
