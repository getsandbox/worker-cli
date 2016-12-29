package com.sandbox.runtime.js.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.runtime.models.jms.JMSRequest;
import com.sandbox.runtime.models.jms.JMSRuntimeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;

/**
 * Created by drew on 6/08/2014.
 */
@Component
public class JMSRequestConverter {

    @Autowired
    private ObjectMapper mapper;

    public JMSRequest fromInstanceJMSRequest(ScriptEngine scriptEngine, JMSRuntimeRequest request) throws Exception {
        return new JMSRequest(scriptEngine, mapper,
                request.getDestination(),
                request.getHeaders(),
                request.getProperties(),
                request.getBody(),
                request.getContentType(),
                request.getIp());
    }
}
