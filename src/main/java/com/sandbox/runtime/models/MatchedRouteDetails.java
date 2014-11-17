package com.sandbox.runtime.models;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class MatchedRouteDetails extends RouteDetails {

    MultivaluedMap<String, String> pathParams;

    public MatchedRouteDetails(RouteDetails routeDetails, MultivaluedMap<String, String> pathParams) {
        super(routeDetails.getMethod(),routeDetails.getPath(), routeDetails.getHeaders());
        //map any extra routedetails props
        setUriTemplate(routeDetails.getUriTemplate());
        setDefineSource(routeDetails.getDefineSource());
        setFunctionSource(routeDetails.getFunctionSource());
        setDefineType(routeDetails.getDefineType());

        this.pathParams = pathParams;
    }

    public MultivaluedMap<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(MultivaluedMap<String, String> pathParams) {
        this.pathParams = pathParams;
    }
}
