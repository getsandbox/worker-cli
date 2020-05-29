package com.sandbox.worker.models.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.worker.models.enums.RuntimeTransportType;

import org.apache.cxf.custom.jaxrs.model.ExactMatchURITemplate;

public interface HTTPRoute extends Route {
    String getMethod();

    void setMethod(String method);

    String getPath();

    ExactMatchURITemplate getPathTemplate();

    void setPath(String path);

    @JsonIgnore
    @Override
    String getProcessingKey();

    @JsonIgnore
    @Override
    String getDisplayKey();

    @Override
    default String getTransport() {
        return RuntimeTransportType.HTTP.name();
    }

    @Override
    default void setTransport(String transport) {
        //noop
    }
}
