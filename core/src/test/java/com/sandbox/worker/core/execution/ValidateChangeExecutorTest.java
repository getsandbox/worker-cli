package com.sandbox.worker.core.execution;

import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.ValidateChangeExecutor;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.InMemoryMetadataService;
import com.sandbox.worker.core.services.LocalFileRepositoryArchiveService;
import com.sandbox.worker.core.services.LocalFileStateService;
import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.test.TestFileUtils;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidateChangeExecutorTest {

    ValidateChangeExecutor executor = new ValidateChangeExecutor();
    SandboxIdentifier sandboxIdentifier = new SandboxIdentifier("1", "1");

    @Test
    void testSimpleSuccess() throws Exception {
        WorkerScriptContext scriptContext = ContextFactory.createContext(
                sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/validateChangeSimpleSuccess")),
                new LocalFileStateService(File.createTempFile("sandbox-state", ".json")),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        );
        executor.doExecute(null, scriptContext);
    }

    @Test
    void testSimpleInvalid() throws Exception {
        WorkerScriptContext scriptContext = ContextFactory.createContext(
                sandboxIdentifier,
                new LocalFileRepositoryArchiveService(TestFileUtils.getFile(this.getClass(),"./core/src/test/resources/validateChangeSimpleInvalid")),
                new LocalFileStateService(File.createTempFile("sandbox-state", ".json")),
                new InMemoryMetadataService(RuntimeVersion.VERSION_3)
        );
        Assertions.assertThrows(ServiceScriptException.class, () -> {
            executor.doExecute(null, scriptContext);
        });

    }
}