package com.sandbox.runtime.models;

import com.sandbox.runtime.models.http.HTTPRouteDetails;
import com.sandbox.runtime.models.http.HttpRuntimeRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class RoutingTable implements Serializable{

    private static final long serialVersionUID = 5416349625258357175L;
    String repositoryId;
    List<RouteDetails> routeDetails;
    private boolean routesSorted = false;

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public List<RouteDetails> getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(List<RouteDetails> routeDetails) {
        this.routeDetails = routeDetails;
    }

    public void addRouteDetails(RouteDetails routeDetails){
        if(getRouteDetails() == null){
            setRouteDetails(new ArrayList<RouteDetails>());
        }

        getRouteDetails().add(routeDetails);

    }

    public HTTPRouteDetails findMatch(String requestMethod, String requestPath, Map<String, String> properties) {
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod(requestMethod);
        request.setUrl(requestPath);
        request.setProperties(properties);
        return (HTTPRouteDetails) findMatch(request);
    }

    public RouteDetails findMatch(RuntimeRequest runtimeRequest){
        List<RouteDetails> routes = getRouteDetails();

        //sort, put the longest route literals at the top, should theoretically be the best matches?!
        if(!routesSorted){
            routes.sort((r1, r2) -> {
                return r2.getProcessingKey().compareTo(r1.getProcessingKey());
            });
            routesSorted = true;
        }

        if(routes == null) return null;

        for(RouteDetails route : routes){
            boolean isMatch = route.matchesRuntimeRequest(runtimeRequest);
            if(isMatch) return route;
        }

        return null;
    }
}
