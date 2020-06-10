package com.sandbox.worker.core.js;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.graal.ValueMapWrapper;
import com.sandbox.worker.core.services.LiquidRenderer;
import com.sandbox.worker.core.services.LiquidRendererException;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.RepositoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HostAccess.Implementable
public class ScriptFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptFunctions.class);
    private final SandboxIdentifier sandboxIdentifier;
    private final RepositoryService repositoryService;
    private LiquidRenderer liquidRenderer;

    public ScriptFunctions(SandboxIdentifier sandboxIdentifier, RepositoryService repositoryService) {
        this.sandboxIdentifier = sandboxIdentifier;
        this.repositoryService = repositoryService;
    }

    public void setLiquidRenderer(LiquidRenderer liquidRenderer) {
        this.liquidRenderer = liquidRenderer;
    }

    @HostAccess.Export
    public String readFile(String filename) {
        return repositoryService.getRepositoryFile(sandboxIdentifier.getFullSandboxId(), filename);
    }

    @HostAccess.Export
    public boolean hasFile(String filename) {
        return repositoryService.hasRepositoryFile(sandboxIdentifier.getFullSandboxId(), filename);
    }

    @HostAccess.Export
    public String readLibrary(String version, String libraryName) throws IOException {
        return IOUtils.toString(ContextFactory.class.getClassLoader().getResource("com/sandbox/runtime/v2/js/" + version + "/" + libraryName).openStream(), "UTF-8");
    }

    @HostAccess.Export
    public String renderLiquid(String templateName, Value templateLocals) throws ServiceScriptException {
        return renderLiquid(templateName, (Object)templateLocals);
    }

    @HostAccess.Export
    public String renderLiquid(String templateName, Object templateLocals) throws ServiceScriptException {
        Map locals = null;
        String result = null;

        //be defensive against crap being passed in, only maps are supported. Could be 'Undefined' or any junk.
        if (templateLocals instanceof Map) {
            locals = (Map) templateLocals;
        } else if(templateLocals instanceof Value){
            locals = ValueMapWrapper.fromValue((Value) templateLocals);
        } else {
            LOG.error("Invalid object passed to render(), return new map, {}", templateLocals.getClass());
            locals = new HashMap<>();
        }

        Supplier<String> templateDataSupplier = () -> readFile("templates/" + templateName + ".liquid");

        if (!hasFile("templates/" + templateName + ".liquid")) {
            throw new ServiceScriptException("Template not found: " + templateName);
        }

        if (locals != null && locals.get("_passUnrenderedTemplate") != null) {
            result = templateDataSupplier.get();
        } else {

            try {
                locals.put("__service", this);

                //pass supplier and template key (based on id of replace-able script functions, will invalidate cache upon file change
                result = liquidRenderer.render("r-" + System.identityHashCode(this) + templateName,
                        templateDataSupplier,
                        locals
                );

            } catch (LiquidRendererException le) {
                throw new ServiceScriptException(le.getMessage(), le);

            } catch (Exception e) {
                //if we get a liquid runtime exception, from our custom tags, then rethrow as a script exception so it gets logged.
                LOG.error("Error rendering template", e);
                throw new ServiceScriptException("Error rendering template", e);
            }
        }

        return result;
    }

}
