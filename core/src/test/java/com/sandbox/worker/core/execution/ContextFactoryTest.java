package com.sandbox.worker.core.execution;

import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.InMemoryStateService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.test.TestFileUtils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContextFactoryTest {

    @Test
    void testVersion1ContextCreation() throws Exception {
        SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("1", "1");
        try(WorkerScriptContext scriptContext = ContextFactory.createContext(sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/generateRoutingTableSimple")),
                new InMemoryStateService(),
                new InMemoryMetadataService(RuntimeVersion.VERSION_1)
        )){
            scriptContext.getExecutionContext().eval("js", "load('main.js')"); //allowed real path

            String exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('missing.js')"); //missing file path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("TypeError: Cannot load script: missing.js", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('/etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: /etc/shadow", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('./../../../etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: ../../../etc/shadow", exceptionMessage);

            assertNotNull(scriptContext.getExecutionContext().eval("js", "_"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "_.each"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "faker.name"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment()"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "amanda"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "amanda('json')"));
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.contains").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.startsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.endsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "[].isLength").toString());
        }
    }

    @Test
    void testVersion2ContextCreation() throws Exception {
        SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("1", "1");
        try(WorkerScriptContext scriptContext = ContextFactory.createContext(sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/generateRoutingTableSimple")),
                new InMemoryStateService(),
                new InMemoryMetadataService(RuntimeVersion.VERSION_2)
        )){
            scriptContext.getExecutionContext().eval("js", "load('main.js')"); //allowed real path

            String exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('missing.js')"); //missing file path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("TypeError: Cannot load script: missing.js", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('/etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: /etc/shadow", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('./../../../etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: ../../../etc/shadow", exceptionMessage);

            assertNotNull(scriptContext.getExecutionContext().eval("js", "_"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "_.each"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "faker.name"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment()"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "amanda"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "amanda('json')"));
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.contains").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.startsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.endsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "[].isLength").toString());
        }
    }

    @Test
    void testVersion3ContextCreation() throws Exception {
        SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("1", "1");
        try(WorkerScriptContext scriptContext = ContextFactory.createContext(sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/generateRoutingTableSimple")),
                new InMemoryStateService(),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        )){
            scriptContext.getExecutionContext().eval("js", "load('main.js')"); //allowed real path

            String exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('missing.js')"); //missing file path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("TypeError: Cannot load script: missing.js", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('/etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: /etc/shadow", exceptionMessage);

            exceptionMessage = null;
            try {
                scriptContext.getExecutionContext().eval("js", "load('./../../../etc/shadow')"); //malicious path
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }
            assertEquals("Error: Read operation is not allowed for: ../../../etc/shadow", exceptionMessage);

            assertNotNull(scriptContext.getExecutionContext().eval("js", "_"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "_.each"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "moment()"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "faker"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "faker.name"));
            assertNotNull(scriptContext.getExecutionContext().eval("js", "new Ajv()"));
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.contains").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.startsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "''.endsWith").toString());
            assertNotEquals("undefined", scriptContext.getExecutionContext().eval("js", "[].isLength").toString());
        }
    }
}