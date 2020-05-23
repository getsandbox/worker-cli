package com.sandbox.worker.core.js;

import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.utils.ErrorUtils;
import com.sandbox.worker.core.exceptions.ServiceScriptException;

public class ValidateChangeExecutor extends AbstractJSExecutor<Void, Void> {

    @Override
    public Void doExecute(Void none, WorkerScriptContext scriptContext) throws ServiceScriptException {
        try {
            bootstrap(scriptContext);
        } catch (Exception e) {
            throw ErrorUtils.getServiceScriptException(e);
        }
        return null;
    }
}
