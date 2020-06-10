package com.sandbox.worker.core.js;

import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.js.models.BodyContentType;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.BodyParserFunction;
import com.sandbox.worker.core.js.models.WorkerHttpRequest;
import com.sandbox.worker.core.js.models.WorkerHttpResponse;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.services.LiquidRenderer;
import com.sandbox.worker.core.services.RuntimeRequestConverter;
import com.sandbox.worker.core.utils.ErrorUtils;
import com.sandbox.worker.core.utils.URLEncodedUtils;
import com.sandbox.worker.core.utils.XMLDoc;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
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
            //not ideal but least worst it seems..
            scriptContext.getScriptFunctions().setLiquidRenderer(liquidRenderer);

            //reset console output, so we start with empty
            scriptContext.getExecutionContextOutput().reset();

            //create parser lambdas for body content, this is bit more complex than expected to support classpath/variable access to netty and graal context objects
            BodyParserFunction<String, Object> bodyParser = request.getContentParser();

            //can override specific types
            if (BodyContentType.JSON.getType().equalsIgnoreCase(request.getContentType())) {
                bodyParser = s -> JSContextHelper.execute(scriptContext, "JSON.parse", s);
            } else if (BodyContentType.URLENCODED.getType().equalsIgnoreCase(request.getContentType())) {
                bodyParser = s -> ProxyObject.fromMap(new HashMap(URLEncodedUtils.decodeBody(s)));
            } else if (BodyContentType.XML.getType().equalsIgnoreCase(request.getContentType())) {
                bodyParser = s -> new XMLDoc(s);
            } else if ((scriptContext.getMetadata().getRuntimeVersion() == RuntimeVersion.VERSION_1 || scriptContext.getMetadata().getRuntimeVersion() == RuntimeVersion.VERSION_2)
                    && BodyContentType.FORMDATA.getType().equalsIgnoreCase(request.getContentType())) {
                //for version 1 or 2 processing don't process form data for backwards compat
                bodyParser = null;
            }
            WorkerHttpRequest httpRequest = RuntimeRequestConverter.fromInstanceHttpRequest(request, bodyParser);
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
        }

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

        } else if (httpResponse.getBody() != null) {
            // treat everything else as plain text
            body = httpResponse.getBody().toString();
        }

        return body;
    }
}
