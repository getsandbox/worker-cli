package com.sandbox.runtime.models;

import org.apache.cxf.jaxrs.model.URITemplate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class RoutingTable implements Serializable{

    private static final long serialVersionUID = 5416349625258357175L;
    String repositoryId;
    List<RouteDetails> routeDetails;

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

    public MatchedRouteDetails findMatch(String requestMethod, String requestPath){
        List<RouteDetails> routes = getRouteDetails();

        //sort, put the longest route literals at the top, should theoretically be the best matches?!
        routes.sort((r1, r2) -> {
            return r2.process().getLiteralChars().compareTo(r1.process().getLiteralChars());
        });

        if(routes == null) return null;

        for(RouteDetails route : routes){
            //if method isnt right, skip!
            if(!route.matchesMethod(requestMethod) ) continue;

            //method matches, so continue..
            URITemplate template = route.process();
            String routeLiterals = template.getLiteralChars();

            MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
            //if we have a match, then set it as the best match, because we could match more than one, we want the BEST match.. which i think should be the one with the shortest 'finalMatchGroup'..
            if( template.match(requestPath, map) ) {
                return new MatchedRouteDetails(route, map);

            }
        }

        return null;
    }
}
