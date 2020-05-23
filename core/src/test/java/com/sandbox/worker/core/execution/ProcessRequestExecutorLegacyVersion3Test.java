package com.sandbox.worker.core.execution;

import com.sandbox.worker.core.services.LocalFileRepositoryService;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.RepositoryService;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.sandbox.worker.test.ProcessRequestExecutorHelper.buildHttp;
import static com.sandbox.worker.test.ProcessRequestExecutorHelper.buildRepo;
import static com.sandbox.worker.test.ProcessRequestExecutorHelper.context;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessRequestExecutorLegacyVersion3Test extends ProcessRequestExecutorLegacyTest {
    @Override
    public RuntimeVersion getExecutionVersion() {
        return RuntimeVersion.VERSION_3;
    }

    @Test
    void testLodashV3LazyEval() throws Exception {
        String mainjs = "var products = [],\n" +
                "    derivePrice1,\n" +
                "    derivePrice2,\n" +
                "    filterByPrice,\n" +
                "    cycleCount = 0;\n" +
                "\n" +
                "derivePrice1 = function (product) {\n" +
                "    product.price += Math.random();\n" +
                "    \n" +
                "    return product;\n" +
                "};\n" +
                "\n" +
                "derivePrice2 = function (product) {\n" +
                "    product.price -= Math.random();\n" +
                "    \n" +
                "    return product;\n" +
                "};\n" +
                "\n" +
                "filterByPrice = function (product) {\n" +
                "    ++cycleCount;\n" +
                "    \n" +
                "    return product.price < 0.5;\n" +
                "};\n" +
                "\n" +
                "products.length = 100000;\n" +
                "\n" +
                "products = _.map(products, function () {\n" +
                "    return {\n" +
                "        price: 1\n" +
                "    };\n" +
                "});\n" +
                "\n" +
                "Sandbox.define(\"/test\", function(req, res) { " +
                "products = _(products)\n" +
                "    .map(derivePrice1)\n" +
                "    .map(derivePrice2)\n" +
                "    .filter(filterByPrice)\n" +
                "    .take(5)\n" +
                "    .value();"  +
                "res.json({ products: products, counter: cycleCount }) })";
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid"), getExecutionVersion()), httpReq);

        assertEquals(200, res.getStatusCode());
    }

    @Disabled
    @Test
    public void handleHttpRequest_amandaTest() throws Exception {
        //no amanda
    }

    @Disabled
    @Test
    public void handleHttpRequest_CannotFindOnObject() throws Exception {
        //no Sandmox
    }

    @Test
    public void handleHttpRequest_checkJsonSchema() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_checkJsonSchema");
        httpReq.setContentType("json");
        httpReq.setBody("{\"id\":\"nick\"}");

        String schema = "{\n" +
                "    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                "    \"title\": \"Product\",\n" +
                "    \"description\": \"A product from Acme's catalog\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"id\": {\n" +
                "            \"description\": \"The unique identifier for a product\",\n" +
                "            \"type\": \"integer\"\n" +
                "        },\n" +
                "        \"name\": {\n" +
                "            \"description\": \"Name of the product\",\n" +
                "            \"type\": \"string\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"required\": [\"id\", \"name\"]\n" +
                "}";

        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.hasRepositoryFile(anyString(), anyString())).thenReturn(true);
        when(repositoryService.getRepositoryFile(anyString(), eq("schemas/request.json"))).thenReturn(schema);
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "schemas/request.json"), getExecutionVersion()), httpReq);

        assertEqualsIW("{\"message\":\"Requesthaserrors\",\"errors\":[{\"param\":\".id\",\"msg\":\"shouldbeinteger\"},{\"param\":\"\",\"msg\":\"shouldhaverequiredproperty'name'\"}]}", res.getBody());
    }

    @Test
    public void handleHttpRequest_runBasicValidationService() throws Exception {
        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicValidationService");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);
        assertEquals(400, res.getStatusCode());
        assertEqualsIW("{\"message\":\"Requesthaserrors\",\"errors\":[{\"param\":\"q\",\"msg\":\"Invalidqueryparam\",\"value\":\"\"}]}", res.getBody());
    }
}
