package com.sandbox.worker.core.js;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.models.WorkerHttpRequest;
import com.sandbox.worker.core.js.models.WorkerHttpResponse;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.LiquidRenderer;
import com.sandbox.worker.core.services.LiquidRendererException;
import com.sandbox.worker.core.services.RuntimeRequestConverter;
import com.sandbox.worker.core.utils.ErrorUtils;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRequestExecutor extends AbstractJSExecutor<HttpRuntimeRequest, HttpRuntimeResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessRequestExecutor.class);

    private final LiquidRenderer liquidRenderer;

    public ProcessRequestExecutor(int maximumRenderBytes) {
        this.liquidRenderer = new LiquidRenderer(maximumRenderBytes);
    }

    @Override
    protected HttpRuntimeResponse doExecute(HttpRuntimeRequest request, WorkerScriptContext scriptContext) throws ServiceScriptException {
        try {
            bootstrap(scriptContext);

            //reset console output, so we start with empty
            scriptContext.getExecutionContextOutput().reset();

            //find matching Sandbox.define(..) function to call, should always match as proxy will guard..
            WorkerHttpRequest httpRequest = RuntimeRequestConverter.fromInstanceHttpRequest(request, s -> JSContextHelper.execute(scriptContext, "JSON.parse", s));
            WorkerHttpResponse httpResponse = new WorkerHttpResponse();

            Value matchedFunction = scriptContext.getScriptObject().getMatchedFunction(request);
            if (matchedFunction == null && "options".equalsIgnoreCase(request.getMethod())) {
                //if its an options request that has failed (is not defined) then send back default OPTIONS response
                return new HttpRuntimeResponse("", 200, null, new HashMap<>(), new ArrayList<>());

            } else if (matchedFunction == null) {
                throw new ServiceScriptException("Could not find a route definition matching your requested route " + request.getMethod() + " " + request.getPath());
            }

            //execute function, have to synchronize to keep graal happy
            JSContextHelper.execute(scriptContext, matchedFunction, httpRequest, httpResponse);

            //possibly persist state, only if its changed and older enough etc
            scriptContext.notifyPossibleStateChange();

            //map response, state, template, activity etc.
            HttpRuntimeResponse response = new HttpRuntimeResponse(
                    getBodyFromResponse(scriptContext, httpRequest, httpResponse),
                    httpResponse.getStatusCode(),
                    httpResponse.getStatusText(),
                    httpResponse.getHeaders(),
                    httpResponse.getCookies()
            );
            response.setResponseDelay(httpResponse.getResponseDelay());
            return response;

        } catch (ServiceScriptException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorUtils.getServiceScriptException(e);
        }
    }

    public String getBodyFromResponse(WorkerScriptContext scriptContext, WorkerHttpRequest httpRequest, WorkerHttpResponse httpResponse) throws ServiceScriptException {
        String body = null;

        // process the response body and build the RuntimeResponse
        if (!httpResponse.isResponseConfigured()) {
            throw new ServiceScriptException("No body has been set in route, you must call one of .json(), .send(), .render() etc");

        } else if (httpResponse.wasRendered()) {

            Supplier<String> templateDataSupplier = () -> scriptContext.getScriptFunctions().readFile("templates/" + httpResponse.getTemplateName() + ".liquid");

            if (!scriptContext.getScriptFunctions().hasFile("templates/" + httpResponse.getTemplateName() + ".liquid")) {
                throw new ServiceScriptException("Template not found: " + httpResponse.getTemplateName());
            }

            Map templateLocals = httpResponse.getTemplateLocals();

            //allow unrendered templates to be passed, special param to support edge cases
            if (templateLocals != null && templateLocals.get("_passUnrenderedTemplate") != null) {
                body = templateDataSupplier.get();
            } else {

                Map<String, Object> locals = new HashMap<>();
                try {
                    locals.put("res", templateLocals);
                    locals.put("req", httpRequest);
                    locals.put("data", templateLocals);
                    locals.put("__service", scriptContext.getScriptFunctions());

                    //pass supplier and template key (based on id of replace-able script functions, will invalidate cache upon file change
                    body = liquidRenderer.render("r-" + System.identityHashCode(scriptContext.getScriptFunctions()) + httpResponse.getTemplateName(),
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

        } else {
            if (httpResponse.getBody() instanceof Value || httpResponse.getBody() instanceof Map || httpResponse.getBody() instanceof Collection) {
                try {
                    // convert JS object to JSON string
                    Value bodyValue = JSContextHelper.execute(scriptContext, "JSON.stringify", httpResponse.getBody());
                    body = bodyValue.asString();
                    if (body == null) {
                        body = bodyValue.toString();
                    }
                } catch (Exception e) {
                    throw new ServiceScriptException("Error serialing body", e);
                }

            } else if(httpResponse.getBody() != null) {
                // treat everything else as plain text
                body = httpResponse.getBody().toString();
            }
        }

        return body;
    }
}
