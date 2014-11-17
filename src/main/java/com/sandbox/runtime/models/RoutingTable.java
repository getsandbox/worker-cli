package com.sandbox.runtime.models;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
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

    public MatchedRouteDetails findMatch(String requestMethod, String requestPath, Map<String, String> headers){
        List<RouteDetails> routes = getRouteDetails();

        //sort, put the longest route literals at the top, should theoretically be the best matches?!
        if(!routesSorted){
            routes.sort((r1, r2) -> {
                return r2.process().getLiteralChars().compareTo(r1.process().getLiteralChars());
            });
            routesSorted = true;
        }

        if(routes == null) return null;
        MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();

        for(RouteDetails route : routes){
            boolean isMatch = route.isMatch(requestMethod, requestPath, pathParams, headers);
            if(isMatch) return new MatchedRouteDetails(route, pathParams);
        }

        return null;
    }
}
