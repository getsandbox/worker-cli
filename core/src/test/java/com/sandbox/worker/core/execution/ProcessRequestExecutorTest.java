package com.sandbox.worker.core.execution;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.models.BodyContentType;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.test.ProcessRequestExecutorHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessRequestExecutorTest {

    ProcessRequestExecutor executor = new ProcessRequestExecutor(1024);

    @Test
    void testSimpleSuccess() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestSimpleSuccess");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/yo");
        Map<String, String> headers = new HashMap<>();
        headers.put("custom-header", "1234");
        request.setHeaders(headers);

        HttpRuntimeResponse response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("1234", response.getHeaders().get("custom-header"));
        assertEquals("{\"a\":1,\"b\":\"yo\",\"d\":{},\"e\":[{},{\"1\":\"abc\"}]}", response.getBody());
    }

    @Test
    void testExceedResourceLimits() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestExceedLimit");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/infinity");

        Exception expectedExp = null;
        try {
            executor.execute(request, scriptContext);
        } catch (ServiceScriptException e) {
            expectedExp = e;
        }
        assertEquals("main.js:2:12 Request has exceeded execution limits", expectedExp.getMessage());
    }

    @Test
    void testPassInvalidJSONAsString() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestSimpleSuccess");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("POST");
        request.setBody("invalid-json");
        request.setUrl("/json");
        request.setContentType(BodyContentType.JSON.name());

        HttpRuntimeResponse response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("invalid-json", response.getBody());
    }

    @Test
    void testLiquidSuccess() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestLiquidSuccess");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/context");

        HttpRuntimeResponse response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("working, hello world", response.getBody());

        request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/nocontext");

        response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("working, ", response.getBody());

        request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/explicit");

        response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("working, hello world", response.getBody());

        Assertions.assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest invalidRequest = new HttpRuntimeRequest();
            invalidRequest.setMethod("GET");
            invalidRequest.setUrl("/invalid");
            executor.execute(invalidRequest, scriptContext);
        });
    }

    @Test
    void testJSONSchemaValidateSuccess() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestJSONSchemaValidateSuccess");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/test");
        request.setContentType(BodyContentType.JSON.getType());
        request.setBody("{\"id\":\"nick\"}");

        HttpRuntimeResponse response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"message\":\"Request has errors\",\"errors\":[{\"param\":\".id\",\"msg\":\"should be integer\"},{\"param\":\"\",\"msg\":\"should have required property 'name'\"}]}", response.getBody());
    }
}