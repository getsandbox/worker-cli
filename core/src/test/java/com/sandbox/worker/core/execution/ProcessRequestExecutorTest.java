package com.sandbox.worker.core.execution;

import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.test.ProcessRequestExecutorHelper;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
    void testJSONSchemaValidateSuccess() throws Exception {
        WorkerScriptContext scriptContext = ProcessRequestExecutorHelper.context("./core/src/test/resources/processRequestJSONSchemaValidateSuccess");
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        request.setMethod("GET");
        request.setUrl("/test");
        request.setContentType("json");
        request.setBody("{\"id\":\"nick\"}");

        HttpRuntimeResponse response = executor.execute(request, scriptContext);
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"message\":\"Request has errors\",\"errors\":[{\"param\":\".id\",\"msg\":\"should be integer\"},{\"param\":\"\",\"msg\":\"should have required property 'name'\"}]}", response.getBody());
    }
}