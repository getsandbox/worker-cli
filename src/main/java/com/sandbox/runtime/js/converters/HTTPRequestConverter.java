package com.sandbox.runtime.js.converters;

import com.sandbox.runtime.models.http.HTTPRequest;
import com.sandbox.runtime.models.http.HttpRuntimeRequest;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;

/**
 * Created by drew on 6/08/2014.
 */
@Component
public class HTTPRequestConverter {

    public HTTPRequest fromInstanceHttpRequest(ScriptEngine scriptEngine, HttpRuntimeRequest request) throws Exception {

        return new HTTPRequest(scriptEngine,
                request.getPath(),
                request.getMethod(),
                request.getHeaders(),
                request.getProperties(),
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
