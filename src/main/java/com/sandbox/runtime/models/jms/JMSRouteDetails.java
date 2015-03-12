package com.sandbox.runtime.models.jms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.RuntimeRequest;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class JMSRouteDetails extends RouteDetails{

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
        return false;
    }

    @Override
    public boolean matchesRuntimeRequest(RuntimeRequest runtimeRequest) {
        return false;
    }

    //matches based on uncompiled path /blah/{smth}
    public boolean matchesEngineRequest(EngineRequest req){
        if(req instanceof JMSRequest){
            return destination.equals(((JMSRequest) req).getDestination());
        }else{
            return false;
        }

    }
}
