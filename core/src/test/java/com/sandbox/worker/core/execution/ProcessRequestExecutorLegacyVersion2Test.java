package com.sandbox.worker.core.execution;

import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.interfaces.RepositoryService;
import com.sandbox.worker.core.services.LocalFileRepositoryService;
import com.sandbox.worker.models.enums.RuntimeVersion;

import org.junit.jupiter.api.Test;

import static com.sandbox.worker.test.ProcessRequestExecutorHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessRequestExecutorLegacyVersion2Test extends ProcessRequestExecutorLegacyTest {
    @Override
    public RuntimeVersion getExecutionVersion() {
        return RuntimeVersion.VERSION_2;
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
}
