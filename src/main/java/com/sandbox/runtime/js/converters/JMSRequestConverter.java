package com.sandbox.runtime.js.converters;

import com.sandbox.runtime.models.jms.JMSRequest;
import com.sandbox.runtime.models.jms.JMSRuntimeRequest;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;

/**
 * Created by drew on 6/08/2014.
 */
@Component
public class JMSRequestConverter {

    public JMSRequest fromInstanceJMSRequest(ScriptEngine scriptEngine, JMSRuntimeRequest request) throws Exception {

        return new JMSRequest(scriptEngine,
                request.getDestination(),
                request.getHeaders(),
                request.getProperties(),
                request.getBody(),
                request.getContentType(),
                request.getIp());
    }
}
