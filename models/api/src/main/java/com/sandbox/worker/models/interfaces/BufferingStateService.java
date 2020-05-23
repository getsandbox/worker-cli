package com.sandbox.worker.models.interfaces;

import java.util.function.Supplier;

public interface BufferingStateService extends StateService {

    //buffer a possible change, but no guarantees its persisted
    void notifyPossibleChange(String sandboxId, Supplier<String> supplier);

    //forces any buffered state for given sandbox to be persisted
    void flush(String sandboxId);

    //forces all buffered state to be persisted
    void flush();

}
