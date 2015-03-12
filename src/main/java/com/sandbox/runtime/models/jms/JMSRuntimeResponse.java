package com.sandbox.runtime.models.jms;

import com.sandbox.runtime.models.RuntimeResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by drew on 6/08/2014.
 */
public class JMSRuntimeResponse extends RuntimeResponse {

    public JMSRuntimeResponse() {
    }

    public JMSRuntimeResponse(String body, int statusCode, Map<String, String> headers, List<String[]> cookies) {
        this.body = body;
        this.headers = headers;
        this.error = null;
    }

    public JMSRuntimeResponse(com.sandbox.runtime.models.Error error) {
        this.error = error;

    }

}
