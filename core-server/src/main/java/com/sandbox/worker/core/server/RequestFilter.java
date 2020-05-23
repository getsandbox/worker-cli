package com.sandbox.worker.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.RouteSupport;
import com.sandbox.worker.core.js.ContextFactory;
import com.sandbox.worker.core.js.GenerateRoutingTableExecutor;
import com.sandbox.worker.core.js.ProcessRequestExecutor;
import com.sandbox.worker.core.js.ScriptFunctions;
import com.sandbox.worker.core.js.models.RouteDetailsProjection;
import com.sandbox.worker.core.js.models.ScriptObject;
import com.sandbox.worker.core.js.models.WorkerHttpRequest;
import com.sandbox.worker.core.js.models.WorkerHttpResponse;
import com.sandbox.worker.core.js.models.WorkerRunnableException;
import com.sandbox.worker.core.js.models.WorkerScriptContext;
import com.sandbox.worker.core.server.exceptions.ServiceOverrideException;
import com.sandbox.worker.core.server.micronaut.ExceptionResponseSupport;
import com.sandbox.worker.core.server.services.HttpMessageConverter;
import com.sandbox.worker.core.utils.EnhancedXMLNode;
import com.sandbox.worker.core.utils.EnhancedXMLNodeList;
import com.sandbox.worker.core.utils.XMLDoc;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import com.sandbox.worker.models.LogActivityMessage;
import com.sandbox.worker.models.RequestActivityMessage;
import com.sandbox.worker.models.RuntimeRequest;
import com.sandbox.worker.models.RuntimeResponse;
import com.sandbox.worker.models.RuntimeTransaction;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.enums.ErrorStrategyEnum;
import com.sandbox.worker.models.enums.RuntimeVersion;
import com.sandbox.worker.models.interfaces.BufferingStateService;
import com.sandbox.worker.models.interfaces.HTTPRoute;
import com.sandbox.worker.models.interfaces.MetadataService;
import com.sandbox.worker.models.interfaces.RepositoryArchiveService;
import com.sandbox.worker.models.interfaces.RoutingTable;
import com.sandbox.worker.models.interfaces.SandboxEventEmitterService;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@TypeHint(
        value = {
                ScriptFunctions.class,
                ScriptObject.class,
                WorkerHttpResponse.class,
                LogActivityMessage.class,
                RequestActivityMessage.class,
                RuntimeTransaction.class,
                RuntimeRequest.class,
                RuntimeResponse.class,
                HttpRuntimeRequest.class,
                HttpRuntimeResponse.class,
                WorkerHttpRequest.class,
                WorkerHttpResponse.class,
                Error.class,
                EnhancedXMLNode.class,
                EnhancedXMLNodeList.class,
                XMLDoc.class
        },
        typeNames = {
                "com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler", // fix for CompileQueue NPE
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", // fix for Provider com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl not found
                "com.sun.org.apache.xerces.internal.dom.NodeImpl",
                "com.sun.org.apache.xml.internal.utils.FastStringBuffer", // fix for 'exception creating new instance for pool'
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                "com.sun.org.apache.xpath.internal.jaxp.XPathImpl",
                "com.sun.org.apache.xpath.internal.functions.FuncBoolean",
                "com.sun.org.apache.xpath.internal.functions.FuncCeiling",
                "com.sun.org.apache.xpath.internal.functions.FuncConcat",
                "com.sun.org.apache.xpath.internal.functions.FuncContains",
                "com.sun.org.apache.xpath.internal.functions.FuncCount",
                "com.sun.org.apache.xpath.internal.functions.FuncCurrent",
                "com.sun.org.apache.xpath.internal.functions.FuncDoclocation",
                "com.sun.org.apache.xpath.internal.functions.FuncExtElementAvailable",
                "com.sun.org.apache.xpath.internal.functions.FuncExtFunction",
                "com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable",
                "com.sun.org.apache.xpath.internal.functions.FuncFalse",
                "com.sun.org.apache.xpath.internal.functions.FuncFloor",
                "com.sun.org.apache.xpath.internal.functions.FuncGenerateId",
                "com.sun.org.apache.xpath.internal.functions.FuncId",
                "com.sun.org.apache.xpath.internal.functions.FuncLang",
                "com.sun.org.apache.xpath.internal.functions.FuncLast",
                "com.sun.org.apache.xpath.internal.functions.FuncLocalPart",
                "com.sun.org.apache.xpath.internal.functions.FuncNamespace",
                "com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace",
                "com.sun.org.apache.xpath.internal.functions.FuncNot",
                "com.sun.org.apache.xpath.internal.functions.FuncNumber",
                "com.sun.org.apache.xpath.internal.functions.FuncPosition",
                "com.sun.org.apache.xpath.internal.functions.FuncQname",
                "com.sun.org.apache.xpath.internal.functions.FuncRound",
                "com.sun.org.apache.xpath.internal.functions.FuncStartsWith",
                "com.sun.org.apache.xpath.internal.functions.FuncString",
                "com.sun.org.apache.xpath.internal.functions.FuncStringLength",
                "com.sun.org.apache.xpath.internal.functions.FuncSubstring",
                "com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter",
                "com.sun.org.apache.xpath.internal.functions.FuncSubstringBefore",
                "com.sun.org.apache.xpath.internal.functions.FuncSum",
                "com.sun.org.apache.xpath.internal.functions.FuncTranslate",
                "com.sun.org.apache.xpath.internal.functions.FuncTrue",
                "com.sun.org.apache.xpath.internal.functions.FuncUnparsedEntityURI",
                "com.sun.org.apache.xpath.internal.operations.And",
                "com.sun.org.apache.xpath.internal.operations.Bool",
                "com.sun.org.apache.xpath.internal.operations.Div",
                "com.sun.org.apache.xpath.internal.operations.Equals",
                "com.sun.org.apache.xpath.internal.operations.Gt",
                "com.sun.org.apache.xpath.internal.operations.Gte",
                "com.sun.org.apache.xpath.internal.operations.Lt",
                "com.sun.org.apache.xpath.internal.operations.Lte",
                "com.sun.org.apache.xpath.internal.operations.Minus",
                "com.sun.org.apache.xpath.internal.operations.Mod",
                "com.sun.org.apache.xpath.internal.operations.Mult",
                "com.sun.org.apache.xpath.internal.operations.Neg",
                "com.sun.org.apache.xpath.internal.operations.NotEquals",
                "com.sun.org.apache.xpath.internal.operations.Number",
                "com.sun.org.apache.xpath.internal.operations.Operation",
                "com.sun.org.apache.xpath.internal.operations.Or",
                "com.sun.org.apache.xpath.internal.operations.Plus",
                "com.sun.org.apache.xpath.internal.operations.Quo",
                "com.sun.org.apache.xpath.internal.operations.String",
                "com.sun.org.apache.xpath.internal.operations.UnaryOperation",
                "com.sun.org.apache.xpath.internal.operations.Variable",
                "com.sun.org.apache.xpath.internal.operations.VariableSafeAbsRef"
        },
        accessType = TypeHint.AccessType.ALL_PUBLIC
)
public abstract class RequestFilter implements HttpServerFilter {

    public static final String MDC_SANDBOX_SANDBOX_ID = "sandboxSandboxId";
    public static final String MDC_SANDBOX_REQUEST_ID = "sandboxRequestId";
    private static final Logger LOG = LoggerFactory.getLogger(RequestFilter.class);
    private static final Pattern IPV4 = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private final GenerateRoutingTableExecutor generateRoutingTableExecutor;
    private final ProcessRequestExecutor processRequestExecutor;
    private final BufferingStateService stateService;
    private final MetadataService metadataService;
    private final RepositoryArchiveService repositoryArchiveService;
    private final SandboxEventEmitterService eventEmitterService;
    private final HttpMessageConverter httpMessageConverter;

    private final ExecutorService ioExecutor;
    private final ScheduledExecutorService delayedResponder = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "delayed-responder"));

    private final ObjectMapper mapper = new ObjectMapper();


    public RequestFilter(SandboxEventEmitterService eventEmitterService, ProcessRequestExecutor processRequestExecutor, BufferingStateService stateService,
                         MetadataService metadataService, RepositoryArchiveService repositoryArchiveService, HttpMessageConverter httpMessageConverter,
                         ExecutorService ioExecutor) {
        this.eventEmitterService = eventEmitterService;
        this.generateRoutingTableExecutor = new GenerateRoutingTableExecutor(RouteDetailsProjection.REQUEST);
        this.processRequestExecutor = processRequestExecutor;
        this.stateService = stateService;
        this.metadataService = metadataService;
        this.repositoryArchiveService = repositoryArchiveService;
        this.httpMessageConverter = httpMessageConverter;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        CompletableFuture<MutableHttpResponse<?>> future = new CompletableFuture<>();
        String hostname = request.getHeaders().get("Host").split(":")[0];
        if (!IPV4.matcher(hostname).matches()) {
            ioExecutor.submit(() -> {
                try {
                    handleRequest(System.currentTimeMillis(), request, future);
                } catch (Throwable t) {
                    LOG.error("Error in doFilter catch-all", t);
                    String sandboxName = getHttpMessageConverter().extractSandboxName(request);
                    future.complete(writeExceptionToResponse(sandboxName, new Exception("Internal error")));
                }
            });
            return Publishers.fromCompletableFuture(future);
        } else {
            return chain.proceed(request);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    //remove?
    protected WorkerScriptContext getLoadedScriptContext(SandboxIdentifier sandboxIdentifier) throws Exception {
        //Get context, will already exist is this isn't first run
        WorkerScriptContext scriptContext = ContextFactory.getOrCreateContext(sandboxIdentifier, repositoryArchiveService, stateService, metadataService);
        if (scriptContext.getRoutingTable() == null) {
            LOG.debug("No routing table found, generating..");
            //need to create routing table, probably first request
            generateRoutingTableExecutor.execute(sandboxIdentifier, scriptContext);
        }
        return scriptContext;
    }

    protected void handleRequestFailure(RuntimeRequest failedRuntimeRequest, Throwable throwable, CompletableFuture<MutableHttpResponse<?>> future) {
        handleRequestFailure(failedRuntimeRequest, throwable, future, null);
    }

    protected void handleRequestFailure(RuntimeRequest failedRuntimeRequest, Throwable throwable, CompletableFuture<MutableHttpResponse<?>> future, String consoleOutput) {
        handleRequestFailure(failedRuntimeRequest, throwable, future, consoleOutput, null);
    }

    protected void handleRequestFailure(RuntimeRequest failedRuntimeRequest, Throwable throwable, CompletableFuture<MutableHttpResponse<?>> future, String consoleOutput, String displayMessage) {
        MDC.put(MDC_SANDBOX_SANDBOX_ID, failedRuntimeRequest.getSandboxId());

        Exception finalException;
        if (!(throwable instanceof ServiceOverrideException)) {
            String finalDisplayMessage;
            if(displayMessage != null) {
                finalDisplayMessage = displayMessage;
            } else if (throwable instanceof WorkerRunnableException){
                finalDisplayMessage = throwable.getMessage();
            } else {
                finalDisplayMessage = "Error processing request";
            }
            finalException = new Exception(finalDisplayMessage);
        } else {
            finalException = (Exception) throwable;
        }

        try {
            //emit activity message for response as well if store is enabled
            if (eventEmitterService != null && failedRuntimeRequest != null) {
                String errorMessage = "Error: " + throwable.getMessage();
                if (consoleOutput == null) {
                    consoleOutput = errorMessage;
                } else {
                    consoleOutput += "\n" + errorMessage;
                }
                eventEmitterService.emitActivity(new RequestActivityMessage(
                        failedRuntimeRequest.getSandboxId(),
                        new RuntimeTransaction(failedRuntimeRequest, consoleOutput.trim(), (RuntimeResponse)null)
                ));
            }
        } catch (Throwable e) {
            LOG.error("Error in failedFunction", e);
        }

        future.complete(writeExceptionToResponse(failedRuntimeRequest.getSandboxName(), finalException));
    }

    protected void recordInternalException(String sandboxName, Exception e) {
        //noop
    }

    protected void handleRequest(long startTime, HttpRequest request, CompletableFuture<MutableHttpResponse<?>> future) throws Exception {
        //map from servlet request to runtime request and validate - This should be common across proxy and runtime
        HttpRuntimeRequest runtimeRequest = null;
        try {
            runtimeRequest = httpMessageConverter.convertRequest(request);
            HttpRuntimeRequest finalRuntimeRequest = runtimeRequest;
            final String requestId = MDC.get(MDC_SANDBOX_REQUEST_ID);

            //allow subclasses to enhance behaviour
            handleHttpRuntimeRequest(request, finalRuntimeRequest);

            //Get context, will already exist if this isn't first run
            SandboxIdentifier sandboxIdentifier = new SandboxIdentifier(finalRuntimeRequest.getSandboxId(), finalRuntimeRequest.getFullSandboxId());
            WorkerScriptContext scriptContext = getLoadedScriptContext(sandboxIdentifier);
            //routing table should exist against executor service if it has been created for this context before
            RoutingTable routingTable = scriptContext.getRoutingTable();

            //Find route for inbound request, fail if not found
            if ("xml".equals(finalRuntimeRequest.getContentType())) {
                try {
                    if (finalRuntimeRequest.getHeaders().get("SOAPAction") != null) {
                        finalRuntimeRequest.getProperties().put("SOAPAction", finalRuntimeRequest.getHeaders().get("SOAPAction"));
                    }

                    String operationName = new XMLDoc(finalRuntimeRequest.getBody()).getSOAPOperationName();
                    finalRuntimeRequest.getProperties().put("SOAPOperationName", operationName);
                    LOG.debug("Found SOAP Operation Name: {}", operationName);

                } catch (Exception e) {
                    LOG.warn("Error retrieving SOAP Operation Name", e);
                }
            }

            HTTPRoute routeMatch = RouteSupport.findMatchedRoute(finalRuntimeRequest, routingTable);
            if (routeMatch == null) {
                //if no route match for given request, then log message and send error response.
                LOG.warn("** Error processing request for {} {} - Invalid route", finalRuntimeRequest.getMethod(), finalRuntimeRequest.getPath() == null ? request.getUri() : finalRuntimeRequest.getPath());
                handleRequestFailure(finalRuntimeRequest, new Exception("Invalid route"), future);
                return;

            } else if (routeMatch.getRouteConfig() != null && routeMatch.getRouteConfig().getErrorStrategy() != ErrorStrategyEnum.NONE) {
                LOG.debug("Applying service override {} for {} {}",
                        routeMatch.getRouteConfig().getErrorStrategy(),
                        finalRuntimeRequest.getMethod(),
                        finalRuntimeRequest.getPath() == null ? request.getUri() : finalRuntimeRequest.getPath()
                );

                if (routeMatch.getRouteConfig().getErrorStrategy() == ErrorStrategyEnum.SERVICE_DOWN) {
                    handleRequestFailure(finalRuntimeRequest, new ServiceOverrideException("Service down", HttpStatus.SERVICE_UNAVAILABLE, ErrorStrategyEnum.SERVICE_DOWN), future);
                    return;

                } else if (routeMatch.getRouteConfig().getErrorStrategy() == ErrorStrategyEnum.TIMEOUT) {
                    delayedResponder.schedule(() -> {
                        handleRequestFailure(finalRuntimeRequest, new ServiceOverrideException("Service down", HttpStatus.GATEWAY_TIMEOUT, ErrorStrategyEnum.TIMEOUT), future);
                    }, 110, TimeUnit.SECONDS); //100 seconds as GLB is 120, so that minus a bit
                    return;

                } else {
                    LOG.warn("Unknown configured service override: {}", routeMatch.getRouteConfig().getErrorStrategy());
                }
            }

            //trigger process request executor, receive runtime response
            logRequest(finalRuntimeRequest, routeMatch);

            //construct function to handle the response from engine and write to response, run on IO executor
            BiConsumer<HttpRuntimeResponse, String> successResponseFunction = (HttpRuntimeResponse runtimeResponse, String consoleOutput) -> {
                try {
                    MDC.put(MDC_SANDBOX_REQUEST_ID, requestId);
                    MDC.put(MDC_SANDBOX_SANDBOX_ID, finalRuntimeRequest.getSandboxId());

                    //set response time so its accurate
                    runtimeResponse.setRespondedTimestamp(System.currentTimeMillis());

                    //map from response back to servlet response - This should be common across proxy and runtime
                    future.complete((MutableHttpResponse<?>) httpMessageConverter.convertResponse(finalRuntimeRequest, runtimeResponse));
                    logConsole(consoleOutput);
                    logResponse(finalRuntimeRequest, runtimeResponse, startTime, scriptContext.getRuntimeVersion());

                    //emit activity message for response as well if store is enabled
                    if (eventEmitterService != null) {
                        eventEmitterService.emitActivity(new RequestActivityMessage(
                                finalRuntimeRequest.getSandboxId(),
                                new RuntimeTransaction(finalRuntimeRequest, consoleOutput, runtimeResponse)
                        ));
                    }
                } catch (Exception e) {
                    handleRequestFailure(finalRuntimeRequest, e, future, consoleOutput);
                    return;
                }
            };

            BiConsumer<Throwable, String> failureResponseFunction = (Throwable t, String consoleOutput) -> {
                handleRequestFailure(finalRuntimeRequest, t, future, consoleOutput);
            };

            //have processed request, fire engine executor
            WorkerRequestRunnable engineTask = new WorkerRequestRunnable(finalRuntimeRequest, routeMatch, successResponseFunction, failureResponseFunction,
                    processRequestExecutor, ioExecutor, delayedResponder, mapper);
            handleEngineRequest(sandboxIdentifier, engineTask);

        } catch (Throwable e) {
            if (!e.getClass().getSimpleName().contains("Suppressed")) LOG.error("Error processing request", e);
            handleRequestFailure(runtimeRequest, e, future);
            return;
        }

    }

    protected void handleHttpRuntimeRequest(HttpRequest request, HttpRuntimeRequest runtimeRequest) throws Exception {
        //noop for overriding
    }

    protected abstract void handleEngineRequest(SandboxIdentifier sandboxIdentifier, WorkerRequestRunnable runnable) throws Exception;

    protected abstract void logRequest(HttpRuntimeRequest request, HTTPRoute matchedRouteDetails);

    protected abstract void logConsole(String output);

    protected abstract void logResponse(HttpRuntimeRequest request, HttpRuntimeResponse response, long startTime, RuntimeVersion runtimeVersion);

    protected MutableHttpResponse<?> writeExceptionToResponse(String sandboxName, Exception exception) {
        recordInternalException(sandboxName, exception);
        return ExceptionResponseSupport.writeExceptionToResponse(mapper, exception);
    }

    protected BufferingStateService getStateService() {
        return stateService;
    }

    protected MetadataService getMetadataService() {
        return metadataService;
    }

    protected SandboxEventEmitterService getEventEmitterService() {
        return eventEmitterService;
    }

    protected RepositoryArchiveService getRepositoryArchiveService() {
        return repositoryArchiveService;
    }

    protected HttpMessageConverter getHttpMessageConverter() {
        return httpMessageConverter;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
