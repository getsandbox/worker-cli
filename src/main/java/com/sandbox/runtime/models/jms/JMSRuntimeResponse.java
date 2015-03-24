package com.sandbox.runtime.models.jms;

import com.sandbox.runtime.models.*;
import com.sandbox.runtime.models.enums.RuntimeTransportType;

import java.util.Map;

/**
 * Created by drew on 6/08/2014.
 */
public class JMSRuntimeResponse extends RuntimeResponse {

    public JMSRuntimeResponse() {
    }

    public JMSRuntimeResponse(String body, Map<String, String> responseHeaders, Map<String, String> requestHeaders, String responseDestination) throws Exception {
        this.body = body;
        this.headers = responseHeaders;
        this.error = null;
        if(responseDestination != null) responseHeaders.put("JMSDestination", responseDestination);
        //if still not set, throw exception
        if(!responseHeaders.containsKey("JMSDestination")) throw new ServiceScriptException("No response queue has been set, either via JMSReplyTo or via send().");
    }

    public JMSRuntimeResponse(com.sandbox.runtime.models.Error error) {
        this.error = error;

    }

    @Override
    public String getTransport() {
        return RuntimeTransportType.JMS.toString();
    }
}
