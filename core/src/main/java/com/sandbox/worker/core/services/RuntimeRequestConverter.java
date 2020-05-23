package com.sandbox.worker.core.services;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.models.WorkerHttpRequest;
import com.sandbox.worker.models.HttpRuntimeRequest;

import java.util.function.Function;

public class RuntimeRequestConverter {

    public static WorkerHttpRequest fromInstanceHttpRequest(HttpRuntimeRequest request, Function<String, Object> jsonParser) throws ServiceScriptException {
        return new WorkerHttpRequest(jsonParser,request.getPath(),
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
