package com.sandbox.runtime.js.converters;

import com.sandbox.runtime.models.HTTPRequest;
import com.sandbox.runtime.models.HttpRuntimeRequest;
import com.sandbox.runtime.models.ServiceScriptException;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;

/**
 * Created by drew on 6/08/2014.
 */
@Component
public class HTTPRequestConverter {

    public HTTPRequest fromInstanceHttpRequest(ScriptEngine scriptEngine, HttpRuntimeRequest request) throws ServiceScriptException {

        return new HTTPRequest(scriptEngine,
                request.getPath(),
                request.getMethod(),
                request.getHeaders(),
                request.getQuery(),
                request.getParams(),
                request.getCookies(),
                request.getBody(),
                request.getContentType(),
                request.getIp(),
                request.getAccepted(),
                request.getUrl());
    }
}
