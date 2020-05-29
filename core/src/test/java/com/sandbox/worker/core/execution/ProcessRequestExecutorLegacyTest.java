package com.sandbox.worker.core.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.InMemoryStateService;
import com.sandbox.worker.core.services.LocalFileRepositoryService;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.RepositoryService;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static com.sandbox.worker.test.ProcessRequestExecutorHelper.buildHttp;
import static com.sandbox.worker.test.ProcessRequestExecutorHelper.buildRepo;
import static com.sandbox.worker.test.ProcessRequestExecutorHelper.context;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ProcessRequestExecutorLegacyTest {

    ObjectMapper mapper = new ObjectMapper();
    ProcessRequestExecutor executor = new ProcessRequestExecutor(1024);

    protected static void assertEqualsIW(String str1, String str2) {
        assertEquals(deleteWhitespace(str1), deleteWhitespace(str2));
    }

    protected HttpRuntimeResponse handleHttpRequest(WorkerScriptContext context, HttpRuntimeRequest request) throws Exception {
        HttpRuntimeResponse execute = executor.execute(request, context);

        context.getStateService().flush();
        return execute;
    }

    public abstract RuntimeVersion getExecutionVersion();

    public WorkerScriptContext getBaseContext() throws Exception {
        return context("./core/src/test/resources/legacyBase", getExecutionVersion());
    }

    @Test
    public void handleHttpRequest_initStateArray() throws Exception {
        InMemoryStateService stateService = new InMemoryStateService("{}");
        String mainjs = "Sandbox.define(\"/test\", function(req, res) {state.bags = []; state.bags.push({ id: 1 + '-broken' }); res.send(JSON.stringify(state)) })";

        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), stateService, getExecutionVersion()), buildHttp("GET", "/test"));

        assertEquals("{\"bags\":[{\"id\":\"1-broken\"}]}", stateService.getSandboxState("1"));
        assertEquals(200, res.getStatusCode());

    }

    @Test
    public void handleHttpRequest_basicDate() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_basicDate");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertNotNull(res.getBody());
        assertNotEquals("undefined", res.getBody());
        assertEquals(200, res.getStatusCode());
        assertTrue(res.getBody().length() > 30);
    }

    @Test
    public void handleHttpRequest_basicFaker() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_basicFaker");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertNotNull(res.getBody());
        assertNotEquals("undefined", res.getBody());
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_fakerRandom() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_fakerRandom");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertNotNull(res.getBody());
        assertNotEquals("undefined", res.getBody());
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_basicMoment() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_basicMoment");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertNotNull(res.getBody());
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicService");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicServiceWithConfigRoute() throws Exception {
        // defines GET /test returns "hello world"
        String mainjs = "Sandbox.define('/'+Sandbox.config.routePath, function(req, res) { res.send(\"hello world\") })";
        Map<String, String> config = new HashMap<>();
        config.put("routePath", "test");
        MetadataService metadataService = new InMemoryMetadataService(getExecutionVersion(), config);
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), metadataService), httpReq);
        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicServiceWithNulls() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicServiceWithNulls");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("{\n" +
                        "  \"blah\" : null,\n" +
                        "  \"msg\" : \"hello world\"\n" +
                        "}",
                res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicPOSTService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("POST", "/handleHttpRequest_runBasicPOSTService");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicHeadersService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicHeadersService");
        httpReq.getProperties().put("SOAPAction", "blah");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runHeadersWithCacheControlService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runHeadersWithCacheControlService");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("max-age: 3600", res.getHeaders().get("Cache-Control"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicSoapService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("POST", "/handleHttpRequest_runBasicSoapService");
        httpReq.getProperties().put("SOAPAction", "blahAction");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicSoapOperationNameService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("POST", "/handleHttpRequest_runBasicSoapOperationNameService");
        httpReq.getProperties().put("SOAPAction", "blahAction");
        httpReq.getProperties().put("SOAPOperationName", "blahOperation");

        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);
        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicValidationService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicValidationService");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        boolean oneMatch = false;
        if (deleteWhitespace("{\n" +
                "  \"message\" : \"Request has errors\",\n" +
                "  \"errors\" : [ {\n" +
                "    \"param\" : \"q\",\n" +
                "    \"msg\" : \"Invalid query param\"\n" +
                "  } ]\n" +
                "}").equals(deleteWhitespace(res.getBody()))) oneMatch = true;

        if (deleteWhitespace("{\n" +
                "  \"message\": \"Request has errors\",\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"param\": \"q\",\n" +
                "      \"msg\": \"Invalid query param\"\n" +
                "    }\n" +
                "  ]\n" +
                "}").equals(deleteWhitespace(res.getBody()))) oneMatch = true;

        if (deleteWhitespace("{\n" +
                "  \"message\" : \"Request has errors\",\n" +
                "  \"errors\" : [ {\n" +
                "    \"param\" : \"q\",\n" +
                "    \"msg\" : \"Invalid query param\",\n" +
                "    \"value\" : null\n" +
                "  } ]\n" +
                "}").equals(deleteWhitespace(res.getBody()))) oneMatch = true;

        assertTrue(oneMatch);
        assertEquals(400, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runInnerScope() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runInnerScope");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("blah", res.getBody());
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runBasicUrlEncodedBody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_runBasicUrlEncodedBody");
        httpReq.setBody("attr=blah&second=qpoklskdjsmth&first=notused&first=1234");
        httpReq.setContentType("urlencoded");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("blah-1234", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_runRootBasicService() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/");
        httpReq.setUrl("/");
        httpReq.setPath("/");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("hello world", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_sendWithJsonObject() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendWithJsonObject");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("{\n" +
                        "  \"username\" : \"nick\",\n" +
                        "  \"firstname\" : \"nick\"\n" +
                        "}",
                res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_sendWithJsonInt() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendWithJsonInt");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
        assertEqualsIW("{\n" +
                "  \"id\" : 1\n" +
                "}", res.getBody());
    }

    @Test
    public void handleHttpRequest_sendWithJsonIntArray() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendWithJsonIntArray");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
        assertEquals(deleteWhitespace("[ {\n" +
                "  \"id\" : 1\n" +
                "} ]"), deleteWhitespace(res.getBody()));
    }

    @Test
    public void handleHttpRequest_sendWithJsonArray() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendWithJsonArray");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[" +
                "  {\n" +
                "    \"username\": \"nick\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"ando\"\n" +
                "  }\n" +
                "]", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_sendWithJsonAssortedObject() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendWithJsonAssortedObject");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[" +
                "  {\n" +
                "    \"username\": \"nick\"\n" +
                "  },\n" +
                "  1,\n" +
                "  \"a string\",\n" +
                "  [\n" +
                "    \"nested\",\n" +
                "    \"array\"\n" +
                "  ]\n" +
                "]", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_jsonWithString() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_jsonWithString");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("this is not json", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_jsonWithFunction() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_jsonWithFunction");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertTrue("undefined".equals(res.getBody()) || "null".equals(res.getBody()) || "{ }".equals(res.getBody()));
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_validXmlBody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validXmlBody");
        httpReq.setContentType("xml");
        httpReq.setBody("<users><user><username>nhoughto</username><firstname>nick</firstname></user></users>");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><users><user><username>nhoughto</username><firstname>nick</firstname></user></users>", res.getBody());
        assertEquals("application/xml", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_validXpath() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validXpath");
        httpReq.setContentType("xml");
        httpReq.setBody("<users><user><username>nhoughto</username><firstname>nick</firstname></user></users>");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals(200, res.getStatusCode());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><user><username>nhoughto</username><firstname>nick</firstname></user>", res.getBody());
    }

    @Test
    public void handleHttpRequest_xmlFindLoop() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_xmlFindLoop");
        httpReq.setContentType("xml");
        httpReq.setBody("<getInventoryRequest>\n" +
                "    <getQuantities>\n" +
                "        <inventoryItemName>8050202001</inventoryItemName>\n" +
                "    </getQuantities>\n" +
                "    <getQuantities>\n" +
                "        <inventoryItemName>8050203001</inventoryItemName>\n" +
                "    </getQuantities>\n" +
                "    <getQuantities>\n" +
                "        <inventoryItemName>8050204001</inventoryItemName>\n" +
                "    </getQuantities>\n" +
                "    <getQuantities>\n" +
                "        <inventoryItemName>8050205001</inventoryItemName>\n" +
                "    </getQuantities>\n" +
                "</getInventoryRequest>");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals(200, res.getStatusCode());
        assertEquals("8050202001805020300180502040018050205001", res.getBody());
    }

    @Test
    public void handleHttpRequest_emptySend() throws Exception {
        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_emptySend");
        httpReq.setContentType("xml");
        httpReq.setBody("<users><user><username>nhoughto</username><firstname>nick</firstname></user></users>");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);
        assertEquals("", res.getBody());
    }

    @Test
    public void handleHttpRequest_invalidXpath2() throws Exception {
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_invalidXpath2");
            httpReq.setContentType("xml");
            httpReq.setBody("<users><user><username>nhoughto</username><firstname>nick</firstname></user></users>");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        ServiceScriptException serviceScriptException = exception;

        assertEquals("main.js:116:13 TypeError: Cannot read property 'substring' of null", serviceScriptException.getMessage());
    }

    @Test
    public void handleHttpRequest_invalidXmlbody() {
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_invalidXmlbody");
            httpReq.setContentType("xml");
            httpReq.setBody("<user><unclosedtag>");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        assertEquals("Can't parse body of type xml", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_xmlNestedXpath() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("POST", "/handleHttpRequest_xmlNestedXpath");
        httpReq.setContentType("xml");
        httpReq.setBody("<PutFTBReport targetNamespace=\"http://vedaleon.com.au/DCSWebServices/ReportServices\">\n" +
                "    <Report>\n" +
                "        <ReportType>FTB</ReportType>\n" +
                "        <AirlineCode>JQ</AirlineCode>>\n" +
                "        <FlightNumber>477</FlightNumber>\n" +
                "        <DeparturePort>NTL</DeparturePort>\n" +
                "        <ArrivalPort>MEL</ArrivalPort>\n" +
                "        <DepartureDate>02/04/2015T14:25</DepartureDate>\n" +
                "        <FirstBoardNow>08:23</FirstBoardNow>\n" +
                "        <Passengers>\n" +
                "            <Passenger>\n" +
                "                <BoardingSequence>1</BoardingSequence>\n" +
                "                <Title>MISS</Title>\n" +
                "                <FirstName>Alison</FirstName>\n" +
                "                <MiddleName>Jane</MiddleName>\n" +
                "                <LastName>Muirhead</LastName>\n" +
                "                <SeatDesignator>13F</SeatDesignator>\n" +
                "                <Reloc>QDR5YT</Reloc>\n" +
                "                <Bags>\n" +
                "                    <Bag>0041686046</Bag>\n" +
                "                </Bags>\n" +
                "            </Passenger>\n" +
                "        </Passengers>\n" +
                "    </Report>\n" +
                "</PutFTBReport>");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_simpleRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/a.liquid"))).thenReturn("{{ data.a }}");

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_simpleRender");
        httpReq.setContentType("json");
        httpReq.setBody("{\"a\":\"b\"}");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid"), getExecutionVersion()), httpReq);

        assertEquals("b", res.getBody());
    }

    @Test
    public void handleHttpRequest_includeRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/a.liquid"))).thenReturn("{% include 'b' %}");
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/b.liquid"))).thenReturn("{{ data.a }}");

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_includeRender");
        httpReq.setContentType("json");
        httpReq.setBody("{\"a\":\"b\"}");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid", "templates/b.liquid"), getExecutionVersion()), httpReq);

        assertEquals("b", res.getBody());
    }

    @Test
    public void handleHttpRequest_exceededRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/a.liquid"))).thenReturn("{% for item in data.hash %}\n{{ item[1] }}\n{% endfor %}");
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_exceededRender");
            httpReq.setContentType("json");
            httpReq.setBody("{\"a\":\"b\"}");
            handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid"), getExecutionVersion()), httpReq);
        });

        assertEquals("Error rendering template", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_invalidIncludeRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/loop.liquid"))).thenReturn("{% include 'wrong' %}");
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/wrong.liquid"))).thenReturn(null);

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_invalidIncludeRender");
            httpReq.setContentType("json");
            httpReq.setBody("{\"a\":\"b\"}");
            handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/loop.liquid", "templates.wrong.liquid"), getExecutionVersion()), httpReq);
        });

        assertEquals("Can't find template: templates/wrong.liquid", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_endlessLoopIncludeRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/loop.liquid"))).thenReturn("{% include 'loop' %}");

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_endlessLoopIncludeRender");
            httpReq.setContentType("json");
            httpReq.setBody("{\"a\":\"b\"}");
            HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/loop.liquid"), getExecutionVersion()), httpReq);
        });
        assertEquals("Stack level too deep! Possible recursive template.", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_loopRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/a.liquid"))).thenReturn("{% for trackingNumber in res.trackingNumbers %}" +
                "<a>{{trackingNumber}}</a>" +
                "{% endfor %}");

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_loopRender");
        httpReq.setContentType("json");
        httpReq.setBody("{\"a\":\"b\"}");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid"), getExecutionVersion()), httpReq);

        assertEquals("<a>a</a><a>b</a><a>c</a>", res.getBody());
    }

    @Test
    public void handleHttpRequest_deepLoopRender() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/a.liquid"))).thenReturn("{% for trackingNumber in res.trackingNumbers %}" +
                "text" +
                "{% for letter in trackingNumber.c %}{{letter}}{% endfor %}" +
                "{% endfor %}");

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_deepLoopRender");
        httpReq.setContentType("json");
        httpReq.setBody("{\"a\":\"b\"}");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "templates/a.liquid"), getExecutionVersion()), httpReq);

        assertEquals("texttexttext123", res.getBody());
    }

    @Test
    public void handleHttpRequest_validJsonObjectbody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validJsonObjectbody");
        httpReq.setContentType("json");
        httpReq.setBody("{\"username\":\"nick\"}");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("{\n" +
                        "  \"username\" : \"nick\"\n" +
                        "}",
                res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
    }

    @Test
    public void handleHttpRequest_testIsJsonBody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_testIsJsonBody");
        httpReq.setContentType("json");
        httpReq.setBody("{\"username\":\"nick\"}");
        httpReq.getHeaders().put("Content-Type", "application/json; charset=UTF8");

        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);
        assertEquals("json", res.getBody());
    }

    @Test
    public void handleHttpRequest_validJsonArraybody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validJsonArraybody");
        httpReq.setContentType("json");
        httpReq.setBody("[{\"username\":\"nick\"},{\"username\":\"ando\"}]");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[" +
                "  {\n" +
                "    \"username\": \"nick\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"ando\"\n" +
                "  }\n" +
                "]", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
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

        assertEqualsIW("{\"message\":\"Requesthaserrors\",\"errors\":[{\"param\":\"id\",\"msg\":\"The‘id’propertymustbean‘integer’.Thetypeofthepropertyis‘string’\"}]}", res.getBody());
    }

    @Test
    public void handleHttpRequest_amandaTest() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_amandaTest");
        httpReq.setContentType("json");
        httpReq.setBody("{\"id\":\"nick\"}");

        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("errors:{\"0\":{\"property\":\"id\",\"propertyValue\":\"nick\",\"attributeName\":\"type\",\"attributeValue\":\"integer\",\"message\":\"The‘id’propertymustbean‘integer’.Thetypeofthepropertyis‘string’\",\"validator\":\"type\",\"validatorName\":\"type\",\"validatorValue\":\"integer\"},\"1\":{\"property\":\"name\",\"attributeName\":\"required\",\"attributeValue\":true,\"message\":\"The‘name’propertyisrequired.\",\"validator\":\"required\",\"validatorName\":\"required\",\"validatorValue\":true},\"2\":{\"property\":\"name\",\"attributeName\":\"type\",\"attributeValue\":\"string\",\"message\":\"The‘name’propertymustbea‘string’.Thetypeofthepropertyis‘undefined’\",\"validator\":\"type\",\"validatorName\":\"type\",\"validatorValue\":\"string\"},\"length\":3,\"errorMessages\":{}}", res.getBody());
    }

    @Test
    public void handleHttpRequest_simpleMomentTest() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_simpleMomentTest");
        httpReq.setContentType("json");
        httpReq.setBody("[{\"username\":\"nick\"},{\"username\":\"ando\"}]");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertNotNull(res.getBody());
    }

    @Test
    public void handleHttpRequest_simpleConsoleTest() throws Exception {
        WorkerScriptContext service = getBaseContext();

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_simpleConsoleTest");
        httpReq.setContentType("json");
        httpReq.setBody("[{\"username\":\"nick\"},{\"username\":\"ando\"}]");
        HttpRuntimeResponse res = handleHttpRequest(service, httpReq);

        assertEquals("console log\n", new String(service.getExecutionContextOutput().toByteArray()));
        assertNotNull(res.getBody());

        res = handleHttpRequest(service, httpReq);
        assertEquals("console log\n", new String(service.getExecutionContextOutput().toByteArray()));
        assertNotNull(res.getBody());
    }

    @Test
    public void handleHttpRequest_validJsonArrayInLodash() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validJsonArrayInLodash");
        httpReq.setContentType("json");
        httpReq.setBody("[{\"username\":\"nick\"},{\"username\":\"ando\"}]");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[" +
                "  {\n" +
                "    \"username\": \"nick\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"username\": \"ando\"\n" +
                "  }\n" +
                "]", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
    }

    @Test
    public void handleHttpRequest_mapLodash() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_mapLodash");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("[2,3,4]", res.getBody());
    }

    @Test
    public void handleHttpRequest_stringifyInternalMap() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_stringifyInternalMap");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("{\"a\":1,\"b\":null}", res.getBody());
    }

    @Test
    public void handleHttpRequest_mapPathParams() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_mapPathParams/{blah}");
        httpReq.setUrl("/handleHttpRequest_mapPathParams/blah");
        httpReq.setParams(new HashMap<>());
        httpReq.getParams().put("blah", "blahparam");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("1", res.getBody());
    }

    @Test
    public void handleHttpRequest_mapLodashObjectEach() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_mapLodashObjectEach");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[\n" +
                "  {\n" +
                "    \"id\": 1234,\n" +
                "    \"name\": \"username\"\n" +
                "  }\n" +
                "]", res.getBody());
    }

    @Test
    public void handleHttpRequest_validJsonAssortedArraybody() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_validJsonAssortedArraybody");
        httpReq.setContentType("json");
        httpReq.setBody("[{\"username\":\"nick\"},1,\"a string\",[\"nested\",\"array\"]]");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEqualsIW("[" +
                "  {\n" +
                "    \"username\": \"nick\"\n" +
                "  },\n" +
                "  1,\n" +
                "  \"a string\",\n" +
                "  [\n" +
                "    \"nested\",\n" +
                "    \"array\"\n" +
                "  ]\n" +
                "]", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
    }

    @Test
    public void handleHttpRequest_setStatus() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_setStatus");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals(400, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_setStatusSeparately() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_setStatusSeparately");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals("test", res.getBody());
        assertEquals(400, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_sendUndefined() throws Exception {

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_sendUndefined");
        HttpRuntimeResponse res = handleHttpRequest(getBaseContext(), httpReq);

        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_underscoreRemoveWeirdness() throws Exception {
        String mainjs = "Sandbox.define(\"/test\", function(req, res) { var smth = [{id:'1'}]; _.remove(smth,function(obj) { return true }); res.send(smth) })";
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        for (int x = 0; x < 3; x++) {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
            HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);
            assertEquals(200, res.getStatusCode());
            assertEqualsIW("[ ]", res.getBody());
        }
    }

    @Test
    public void handleHttpRequest_underscorePullWeirdness() throws Exception {
        String mainjs = "Sandbox.define(\"/test\", function(req, res) { \n" +
                "var array = [1, 2, 3, 1, 2, 3];\n" +
                "         _.pull(array, 2, 3);\n" +
                "         res.send(array);})";
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = null;

        for (int x = 0; x < 3; x++) {
            res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);
            assertEquals(200, res.getStatusCode());
            assertEqualsIW("[ 1, 1 ]", res.getBody());
        }
    }

    @Test
    public void handleHttpRequest_reassignUnderscore() throws Exception {
        String mainjs = "Sandbox.define(\"/test\", function(req, res) { _.each = function() {  }; res.send('blah') });\n Sandbox.define(\"/test2\", function(req, res) { var array = ['response']; _.each(array, function(item){ res.send(item) }) });";
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test2");

        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);
        assertEquals("response", res.getBody());
    }

    @Test
    public void testengine_tryQuitEngine() throws Exception {
        String mainjs = "Sandbox.define(\"/test\", function(req, res) { res.send({quit: typeof quit, exit: typeof exit, load: typeof load, loadWithNewGlobal: typeof loadWithNewGlobal, exec: typeof $EXEC})  });";
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);

        //this calls prepare on the engine, clearing the scope/bindings any junk the user might have put in
        ObjectNode response = mapper.readValue(res.getBody(), ObjectNode.class);
        assertEquals("undefined", response.get("quit").textValue());
        assertEquals("undefined", response.get("exit").textValue());
        assertEquals("undefined", response.get("loadWithNewGlobal").textValue());
        assertEquals("undefined", response.get("exec").textValue());

    }

    @Test
    public void handleHttpRequest_CannotFindOnObject() throws Exception {
        String state = "{\"accounts\":[{\"id\":\"0000500953\",\"orders\":[{\"order_id\":\"eb80c6a976c50f3f\",\"order_creation_date\":\"2014-09-17T02:17:04-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"428e2b566a85def8\",\"order_creation_date\":\"2014-09-17T02:19:13-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"068950b4768c7ff7\",\"order_creation_date\":\"2014-09-17T02:20:46-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"ad039d7b565b8cc0\",\"order_creation_date\":\"2014-09-17T02:30:03-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"b94c386cf7d4eb01\",\"order_creation_date\":\"2014-09-17T02:31:33-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"1c0f77b6273e73a9\",\"order_reference\":\"hello world\",\"order_creation_date\":\"2014-09-17T02:34:41-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"811dc67dedf0715b\",\"order_creation_date\":\"2014-09-17T02:34:42-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"d112d9455aded051\",\"order_reference\":\"hello world\",\"order_creation_date\":\"2014-09-17T02:35:27-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"e07b2d97703443d2\",\"order_creation_date\":\"2014-09-17T02:37:40-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},{\"order_id\":\"9d5fedd16b14105b\",\"order_creation_date\":\"2014-09-17T02:38:52-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null,null]},null,{\"order_id\":\"bd009c320c135dc9\",\"order_creation_date\":\"2014-09-17T02:39:59-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null,null]},null,{\"order_id\":\"b35fd9721686e02b\",\"order_creation_date\":\"2014-09-17T02:51:23-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":0},\"shipments\":[null]},null,{\"order_id\":\"84d536fcd3f5b077\",\"order_creation_date\":\"2014-09-17T02:56:30-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":1},\"shipments\":[{\"shipment_id\":\"6fb020ce938cb1a9f10c29f1\",\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Initiated\",\"number_of_items\":6}}]},{\"order_id\":\"ca845c4917dd0f6f\",\"order_creation_date\":\"2014-09-17T03:00:41-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":1},\"shipments\":[{\"shipment_id\":\"deb429a13423a980546d40c1\",\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Initiated\",\"number_of_items\":3}}]},{\"order_id\":\"4eff5315bfeceff0\",\"order_creation_date\":\"2014-09-17T03:01:12-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":1},\"shipments\":[{\"shipment_id\":\"deb429a13423a980546d40c1\",\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Initiated\",\"number_of_items\":3}}]},{\"order_id\":\"b9f46855d84f7c54\",\"order_creation_date\":\"2014-09-17T03:01:20-04:00\",\"order_summary\":{\"total_cost\":\"126.2\",\"total_gst\":\"11.47\",\"status\":\"Initiated\",\"number_of_shipments\":1},\"shipments\":[{\"shipment_id\":\"deb429a13423a980546d40c1\",\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Initiated\",\"number_of_items\":3}}]}],\"shipments\":[{\"shipment_id\":\"b60272c156098b1613419f81\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:16:46-04:00\",\"items\":[{\"item_id\":\"ca7e6191963b5fbe9d68b3b8\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"eaa8d0f1b8876fe9d4175430\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"21d87b5d185abbbf8398e731\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"eb40aad807e5f4b346e80d1f\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:16:46-04:00\",\"items\":[{\"item_id\":\"1dcb89fcfb3a1c4bd0fa1d00\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"4651e3e09881e89676db9416\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"e44b743b70bc3e513c2d147c\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"e961ef7f69917c7d34ef651d\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:17:48-04:00\",\"items\":[{\"item_id\":\"76dcd105aaa498c489763e61\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"b2ed0bce6160b7ff986d64a6\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"8fd07b2b559048c7243d6c82\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"0bab8010d75b7aeb953a5704\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:17:48-04:00\",\"items\":[{\"item_id\":\"742e7d408c0c8edf5aa7ad28\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"ab516b853a68f6deacf298bd\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"8fa1d2d9a0a4b138ff364397\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"a25d959bd020b531554d0432\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:22:38-04:00\",\"items\":[{\"item_id\":\"5cf64ea34603bf3c473fe1be\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"dd70245a17ee568ba42294da\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"9f4c3e582e9762b253a34c71\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"7be6c890c5ec71f7595b6643\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:34:30-04:00\",\"items\":[{\"item_id\":\"177ce5d38a90e866a6c55b0b\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"b9fee646baf254efea76d2a7\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"023b6a48aa6ed3030645eabb\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"2e187f70efbddce529dc28a5\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:34:30-04:00\",\"items\":[{\"item_id\":\"4145f2742dfc9b1a4d26bca9\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"d35e97466c74810940dd605b\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"69818b2e6f98d41f22dfbdfc\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null,null,null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},null,null,{\"shipment_id\":\"81656c4177564b7c9735e715\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:43:16-04:00\",\"items\":[{\"item_id\":\"eb80529dd3bbb06189e41015\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"0a06635f8d9684e5458887f3\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"9a4938db77a6919e3f3170d6\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"0d1bb31d6540d726ef2212cc\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:43:16-04:00\",\"items\":[{\"item_id\":\"237689af2bbd95285f237ece\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"dfa70410b152974403add291\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"45ba80946e841e64cae7c093\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},null],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},null,{\"shipment_id\":\"eadc913b6e8912cfe9e8fd94\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:57:27-04:00\",\"items\":[{\"item_id\":\"6c348163306baec19870d370\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"ff581580b29e5d3fc95b69a2\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"ce3e9972f3d9e6a036b1fe4d\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}}],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"bc0c8ffc73677f0181b4c31e\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:57:29-04:00\",\"items\":[{\"item_id\":\"fca77e29a02d35754c60a3fa\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"8e003f02399f1fdb32247a9e\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"f8a35a633d89d69cacb501f9\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}}],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"deb429a13423a980546d40c1\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:58:37-04:00\",\"items\":[{\"item_id\":\"ba1be5e7bbd4254ca53bf473\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"f56809d9ad177920a3b7e76b\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"3c0ad17415df54cac604cd99\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}}],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}},{\"shipment_id\":\"670b8b7368495ea03b93dea9\",\"shipment_reference\":\"My second shipment ref\",\"shipment_creation_date\":\"2014-09-17T02:58:37-04:00\",\"items\":[{\"item_id\":\"f937ee3a4b6c228baa91a156\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"5910a24d83e8bc624b0a4dde\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}},{\"item_id\":\"4618bd2addcdb8adbc018389\",\"item_reference\":\"blocked\",\"product_id\":\"T28S\",\"item_summary\":{\"total_cost\":\"20.7\",\"total_gst\":\"1.88\",\"status\":\"Created\"}}],\"shipment_summary\":{\"total_cost\":\"63.1\",\"total_gst\":\"5.74\",\"status\":\"Created\",\"number_of_items\":3}}]},null,null,null]}";
        String mainjs = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyPcc/main.js"));
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

        BufferingStateService stateService = new InMemoryStateService(state);

        HttpRuntimeRequest httpReq = buildHttp("POST", "/shipments/dispatch/order");
        httpReq.getHeaders().put("account-number", "0000500953");
        httpReq.setBody("{\n" +
                "    \"account_number\": \"0000500953\",\n" +
                "    \"shipments\": [\n" +
                "        {\n" +
                "            \"shipment_id\": \"deb429a13423a980546d40c1\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        httpReq.setContentType("json");

        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), stateService, getExecutionVersion()), httpReq);
        assertEquals(201, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_noResBodyException() {
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_noResBodyException");
            handleHttpRequest(getBaseContext(), httpReq);
        });

        assertEquals("No body has been set in route, you must call one of .json(), .send(), .render() etc", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_BadJSErrorException() throws Exception {
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            // defines GET /test returns "hello world"
            String mainjs = "Sandbox.define(\"/test\", function(req, res) { console.log('meep'); var blah = {shipmentId: shipmentIds)";
            RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
            when(repositoryService.getRepositoryFile(anyString(), anyString())).thenReturn(mainjs);

            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_BadJSErrorException");
            handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);

        });
        assertEquals("main.js:1:102 SyntaxError: main.js:1:101 Expected comma but found )\n" +
                "Sandbox.define(\"/test\", function(req, res) { console.log('meep'); var blah = {shipmentId: shipmentIds)\n" +
                "                                                                                                     ^\n" +
                "main.js:1:102 Expected } but found eof\n" +
                "Sandbox.define(\"/test\", function(req, res) { console.log('meep'); var blah = {shipmentId: shipmentIds)\n" +
                "                                                                                                      ^\n", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_ReferenceErrorException() {

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_ReferenceErrorException");
            handleHttpRequest(getBaseContext(), httpReq);
        });

        assertEquals("main.js:296:128 ReferenceError: shipmentIds is not defined", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_TypeErrorException() {

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_TypeErrorException");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        assertEquals("main.js:299:116 TypeError: blah.substring is not a function", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_RangeErrorException() {

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_RangeErrorException");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        assertEquals("RangeError: Invalid array length", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_URIErrorException() {

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_URIErrorException");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        assertEquals("URIError: illegal escape sequence", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_noMatchingTemplateException() {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("templates/blah.liquid"))).thenReturn(null);

        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_noMatchingTemplateException");
            handleHttpRequest(getBaseContext(), httpReq);
        });
        assertEquals("Template not found: blah", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_requireFile() throws Exception {

        String mainjs = "var child = require('./child.js');";
        String childjs = "Sandbox.define(\"/test\", function(req, res) { res.send('ok'); })";

        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(mainjs);
        when(repositoryService.getRepositoryFile(anyString(), eq("child.js"))).thenReturn(childjs);
        when(repositoryService.hasRepositoryFile(anyString(), anyString())).thenReturn(true);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "child.js"), getExecutionVersion()), httpReq);

        assertEquals("ok", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_requireJSONFile() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        String childjs = "{\"A\":2}";
        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("legacyBase/main.js"), "UTF-8"));
        when(repositoryService.getRepositoryFile(anyString(), eq("child.json"))).thenReturn(childjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/handleHttpRequest_requireJSONFile");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "child.json"), getExecutionVersion()), httpReq);

        assertEqualsIW("{\"A\":2}", res.getBody());
        assertEquals("application/json", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_requireInvalidFile() {
        ServiceScriptException exception = assertThrows(ServiceScriptException.class, () -> {
            RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
            String mainjs = "var child = require('./child.js');";

            when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(mainjs);
            when(repositoryService.getRepositoryFile(anyString(), eq("child.js"))).thenReturn(null);

            HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
            handleHttpRequest(context(buildRepo(repositoryService, "main.js", "child.js"), getExecutionVersion()), httpReq);
        });

        assertEquals("Could not find a route definition matching your requested route GET null", exception.getMessage());
    }

    @Test
    public void handleHttpRequest_weirdPathsRequireFile() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        String mainjs = "var child = require('./routes/../routes/.././routes/child.js');";
        String childjs = "Sandbox.define(\"/test\", function(req, res) { res.send('ok23'); })";

        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(mainjs);
        when(repositoryService.getRepositoryFile(anyString(), eq("routes/child.js"))).thenReturn(childjs);
        when(repositoryService.hasRepositoryFile(anyString(), anyString())).thenReturn(true);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "routes/child.js"), getExecutionVersion()), httpReq);

        assertEquals("ok23", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_requireFileWithFunctions() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        String mainjs = "var child = require('./child.js'); Sandbox.define(\"/test\", function(req, res) { child.respond(res); })";
        String childjs = "var result = 'blah'; if (typeof __g_load !== 'undefined') { result = 'found __g_load' } else { result = '__g_load NOT EXIST' }; module.exports.respond = function(res) { res.send(result) }";

        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(mainjs);
        when(repositoryService.getRepositoryFile(anyString(), eq("child.js"))).thenReturn(childjs);
        when(repositoryService.hasRepositoryFile(anyString(), anyString())).thenReturn(true);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js", "child.js"), getExecutionVersion()), httpReq);

        assertEquals("__g_load NOT EXIST", res.getBody());
        assertEquals("text/plain", res.getHeaders().get("Content-Type"));
        assertEquals(200, res.getStatusCode());
    }

    @Test
    public void handleHttpRequest_tryOutConsole() throws Exception {
        RepositoryService repositoryService = mock(LocalFileRepositoryService.class);
        String mainjs = "console.log('testing console.log %j this object', {x: 1}); Sandbox.define('/test', function(req, res) { res.send('ok'); })";

        when(repositoryService.getRepositoryFile(anyString(), eq("main.js"))).thenReturn(mainjs);

        HttpRuntimeRequest httpReq = buildHttp("GET", "/test");
        HttpRuntimeResponse res = handleHttpRequest(context(buildRepo(repositoryService, "main.js"), getExecutionVersion()), httpReq);

        assertEquals(200, res.getStatusCode());
    }

}
