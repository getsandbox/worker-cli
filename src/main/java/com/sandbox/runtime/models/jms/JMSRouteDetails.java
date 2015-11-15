package com.sandbox.runtime.models.jms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.common.models.jms.JMSRuntimeRequest;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.common.models.RuntimeRequest;

import java.util.Map;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class JMSRouteDetails extends RouteDetails{

    public JMSRouteDetails() {
    }

    public JMSRouteDetails(String destination, Map<String, String> properties) {
        this.destination = destination;
        setProperties(properties);
    }

    String destination;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonIgnore
    @Override
    public String getProcessingKey() {
        return getDestination();
    }

    @JsonIgnore
    @Override
    public String getDisplayKey() {
        return getDestination();
    }

    @Override
    public boolean matchesRoute(RouteDetails otherRoute) {
        return otherRoute instanceof JMSRouteDetails && getDestination().equals(((JMSRouteDetails) otherRoute).getDestination());
    }

    @Override
    public boolean matchesRuntimeRequest(RuntimeRequest runtimeRequest) {
        return runtimeRequest instanceof JMSRuntimeRequest && getDestination().equals(((JMSRuntimeRequest) runtimeRequest).getDestination());
    }

    //matches based on uncompiled path /blah/{smth}
    public boolean matchesEngineRequest(EngineRequest req){
        return req instanceof JMSRequest && getDestination().equals(((JMSRequest)req).destination());
    }
}
