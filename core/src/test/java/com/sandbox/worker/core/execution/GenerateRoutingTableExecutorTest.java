package com.sandbox.worker.core.execution;

import com.sandbox.worker.core.js.models.RouteDetailsProjection;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.GenerateRoutingTableExecutor;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.core.services.LocalFileStateService;
import com.sandbox.worker.models.interfaces.Route;
import com.sandbox.worker.models.interfaces.RoutingTable;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.test.TestFileUtils;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateRoutingTableExecutorTest {

    SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("1", "1");
    GenerateRoutingTableExecutor executor = new GenerateRoutingTableExecutor(RouteDetailsProjection.EDITOR);

    @Test
    void testSimple() throws Exception {
        WorkerScriptContext scriptContext = ContextFactory.createContext(
                sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(), "./core/src/test/resources/generateRoutingTableSimple")),
                new LocalFileStateService(File.createTempFile("sandbox-state", ".json")),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        );
        RoutingTable table = executor.execute(sandboxIdentifier, scriptContext);
        assertEquals("1", table.getRepositoryId());
        List<Route> routes = table.getRouteDetails();
        assertEquals(1, routes.size());

        Route route = routes.get(0);
        assertEquals("define", route.getDefinitionSource().getDefineType());
        assertEquals("/yoGET", route.getDisplayKey());
        assertEquals("http", route.getTransport());

        assertTrue(route.getDefinitionSource().getFunctionSource().getPath().endsWith("main.js"));
        assertEquals(1, route.getDefinitionSource().getFunctionSource().getLineNumber());
        assertEquals("function(req, res){\n" +
                "    res.send('yo')\n" +
                "}", route.getDefinitionSource().getFunctionSource().getImplementation());

        assertTrue(route.getDefinitionSource().getDefineSource().getPath().endsWith("main.js"));
        assertEquals(1, route.getDefinitionSource().getDefineSource().getLineNumber());

    }

    @Test
    void testRequires() throws Exception{
        WorkerScriptContext scriptContext = ContextFactory.createContext(
                sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/generateRoutingTableRequires")),
                new LocalFileStateService(File.createTempFile("sandbox-state", ".json")),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        );
        RoutingTable table = executor.execute(sandboxIdentifier, scriptContext);
        assertEquals("1", table.getRepositoryId());
        List<Route> routes = table.getRouteDetails();
        assertEquals(1, routes.size());

        Route route = routes.get(0);
        assertEquals("define", route.getDefinitionSource().getDefineType());
        assertEquals("/yoGET", route.getDisplayKey());
        assertEquals("http", route.getTransport());

        assertTrue(route.getDefinitionSource().getFunctionSource().getPath().endsWith("route.js"));
        assertEquals(3, route.getDefinitionSource().getFunctionSource().getLineNumber());
        assertEquals("function(req, res){\n" +
                "   res.send('yo')\n" +
                "}", route.getDefinitionSource().getFunctionSource().getImplementation());

        assertTrue(route.getDefinitionSource().getDefineSource().getPath().endsWith("main.js"));
        assertEquals(5, route.getDefinitionSource().getDefineSource().getLineNumber());

    }

    @Test
    void testImports() throws Exception{
        WorkerScriptContext scriptContext = ContextFactory.createContext(
                sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/generateRoutingTableImports")),
                new LocalFileStateService(File.createTempFile("sandbox-state", ".json")),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        );
        RoutingTable table = executor.execute(sandboxIdentifier, scriptContext);
        assertEquals("1", table.getRepositoryId());
        List<Route> routes = table.getRouteDetails();
        assertEquals(1, routes.size());

        Route route = routes.get(0);
        assertEquals("define", route.getDefinitionSource().getDefineType());
        assertEquals("/yoGET", route.getDisplayKey());
        assertEquals("http", route.getTransport());

        assertTrue(route.getDefinitionSource().getFunctionSource().getPath().endsWith("route.mjs"));
        assertEquals(1, route.getDefinitionSource().getFunctionSource().getLineNumber());
        assertEquals("(req, res) => res.send('yo')\n", route.getDefinitionSource().getFunctionSource().getImplementation());

        assertTrue(route.getDefinitionSource().getDefineSource().getPath().endsWith("main.mjs"));
        assertEquals(3, route.getDefinitionSource().getDefineSource().getLineNumber());

    }
}