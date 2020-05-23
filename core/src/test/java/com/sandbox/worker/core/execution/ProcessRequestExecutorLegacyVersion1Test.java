package com.sandbox.worker.core.execution;

import com.sandbox.worker.models.enums.RuntimeVersion;

public class ProcessRequestExecutorLegacyVersion1Test extends ProcessRequestExecutorLegacyTest {
    @Override
    public RuntimeVersion getExecutionVersion() {
        return RuntimeVersion.VERSION_1;
    }
}
