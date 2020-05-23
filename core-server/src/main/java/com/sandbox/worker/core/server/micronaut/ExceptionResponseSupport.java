package com.sandbox.worker.core.server.micronaut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.worker.core.server.exceptions.ServiceOverrideException;
import com.sandbox.worker.models.Error;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;

import java.util.Arrays;

public class ExceptionResponseSupport {

    public static MutableHttpResponse<?> writeCatchAllToResponse(ObjectMapper mapper) {
        return writeExceptionToResponse(mapper, new Exception("Error processing request"));
    }

    public static MutableHttpResponse<?> writeExceptionToResponse(ObjectMapper mapper, Exception exception) {
        if (exception instanceof ServiceOverrideException) {
            return HttpResponse.status(((ServiceOverrideException) exception).getStatus(), exception.getMessage());
        } else {
            return writeExceptionToResponse(mapper, HttpStatus.INTERNAL_SERVER_ERROR, null, exception);
        }
    }

    public static MutableHttpResponse<?> writeExceptionToResponse(ObjectMapper mapper, HttpStatus status, String reason, Exception exception) {
        ObjectNode errorWrapper = mapper.createObjectNode();
        ObjectNode error = mapper.convertValue(new Error(exception.getMessage()), ObjectNode.class);
        errorWrapper.set("errors", mapper.convertValue(Arrays.asList(error), ArrayNode.class));

        return HttpResponse.status(status, reason)
                .contentType("application/json")
                .body(errorWrapper.toString());
    }
}
