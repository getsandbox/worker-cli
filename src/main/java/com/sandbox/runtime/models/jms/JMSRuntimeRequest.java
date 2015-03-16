package com.sandbox.runtime.models.jms;

import com.sandbox.runtime.models.RuntimeRequest;
import com.sandbox.runtime.models.enums.RuntimeTransportType;

/**
 * Created by nickhoughton on 1/08/2014.
 */
public class JMSRuntimeRequest extends RuntimeRequest {

    String destination;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getTransport() {
        return RuntimeTransportType.JMS.toString();
    }
}
