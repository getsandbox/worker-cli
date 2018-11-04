package com.sandbox.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.runtime.converters.HttpServletConverter;
import com.sandbox.runtime.js.converters.HTTPRequestConverter;
import com.sandbox.runtime.js.models.Console;
import com.sandbox.runtime.js.services.JSEngineService;
import com.sandbox.runtime.js.services.RuntimeService;
import com.sandbox.runtime.js.services.ServiceManager;
import com.sandbox.runtime.models.ActivityMessage;
import com.sandbox.runtime.models.ActivityMessageTypeEnum;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.RoutingTable;
import com.sandbox.runtime.models.RoutingTableCache;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.RuntimeTransaction;
import com.sandbox.runtime.models.XMLDoc;
import com.sandbox.runtime.models.config.RuntimeConfig;
import com.sandbox.runtime.models.http.HTTPRequest;
import com.sandbox.runtime.models.http.HTTPRoute;
import com.sandbox.runtime.models.http.HttpRuntimeRequest;
import com.sandbox.runtime.models.http.HttpRuntimeResponse;
import com.sandbox.runtime.services.InMemoryActivityStore;
import com.sandbox.runtime.services.RouteConfigUtils;
import com.sandbox.runtime.utils.FormatUtils;
import com.sandbox.runtime.utils.MapUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

@Component
@Lazy
public class HttpRequestHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MapUtils mapUtils;

    @Autowired
    FormatUtils formatUtils;

    @Autowired
    RoutingTableCache cache;

    @Autowired
    RuntimeConfig config;

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    private JSEngineService engineService;

    @Autowired
    private HttpServletConverter servletConverter;

    @Autowired
    private HTTPRequestConverter serviceConverter;

    @Autowired
    private InMemoryActivityStore activityStore;

    private Map<Long, AsyncContext> delayedRequests = new ConcurrentHashMap<>();

    private Map<FutureTask, AsyncContext> runningRequests = new ConcurrentHashMap<>();

    private ExecutorService engineExecutor = Executors.newSingleThreadExecutor();

    private AtomicInteger messageIdCounter = new AtomicInteger(0);

    //defaulted
    public static final String SANDBOX_ID = "1";

    public HttpRequestHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                for (Long time : delayedRequests.keySet()){
                    if(currentTime > time){
                        AsyncContext asyncContext = delayedRequests.remove(time);
                        asyncContext.complete();
                    }
                }
            } catch (Exception e){
                logger.error("Error completing async context", e);
            }

        }, 0, 10, TimeUnit.MILLISECONDS);

        Executors.newSingleThreadExecutor().submit(() -> {
            while(true){
                try {
                    for (FutureTask future : runningRequests.keySet()) {
                        if (future.isDone() || future.isCancelled()) {
                            //future is complete, so remove it
                            AsyncContext asyncContext = runningRequests.remove(future);

                            //if the future result is true, then it is being completed in another async manner and should be ignored
                            try {
                                if ((Boolean) future.get()) return;
                            } catch (Exception e) {
                                logger.error("Error completing future", e);
                            }

                            //otherwise complete it
                            asyncContext.complete();
                        }
                    }

                    Thread.sleep(5);

                } catch (Exception e){
                    logger.error("Error completing async context", e);
                }
            }

        });

    }

    //handle is synchronized so that the JS processing is done on one thread.
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        baseRequest.setHandled(true);

        try {
            StopWatch requestTimer = new StopWatch();
            requestTimer.start();

            //convert incoming request to InstanceHttpRequest
            HttpRuntimeRequest runtimeRequest = servletConverter.httpServletToInstanceHttpRequest(request);

            //create and lookup routing table
            RoutingTable routingTable = getOrCreateRoutingTable();
            HTTPRoute routeMatch = findMatchedRoute(runtimeRequest, routingTable);

            if(routeMatch == null) {
                //if no route match for given request, then log message and send error response.
                logger.warn("** Error processing request for {} {} - Invalid route", runtimeRequest.getMethod(), runtimeRequest.getPath() == null ? request.getRequestURI() : runtimeRequest.getPath());
                response.setStatus(500);
                response.setHeader("Content-Type", "application/json");
                response.getWriter().write(convertExceptionMessageToResponse("Invalid route"));
                return;
            }

            //found valid request with route, so process it and write response.
            processValidRequest(request, response, runtimeRequest, routeMatch, requestTimer);

        } catch (Exception e) {
            logger.error("Error processing request", e);

            //write out error as json
            response.setStatus(500);
            response.setHeader("Content-Type","application/json");
            response.getWriter().write(convertExceptionToResponse(e));
        }
    }

    private void processValidRequest(HttpServletRequest request, HttpServletResponse response, HttpRuntimeRequest runtimeRequest, HTTPRoute routeMatch, StopWatch requestTimer) {
        //log request with route
        logRequest(runtimeRequest, routeMatch);

        final AsyncContext asyncContext = request.startAsync();
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            boolean doneAsync = false;

            try {
                HttpRuntimeResponse runtimeResponse = null;
                Console runtimeConsole = null;

                if("OPTIONS".equalsIgnoreCase(runtimeRequest.getMethod())){
                    //if options request, send back CORS headers
                    runtimeResponse = new HttpRuntimeResponse("", 200, null, new HashMap<>(), new ArrayList<>());
                }else{
                    //otherwise process normally
                    HTTPRequest httpRequest = serviceConverter.fromInstanceHttpRequest(engineService.getScriptEngine(), runtimeRequest);

                    RuntimeService runtimeService = getRuntimeService();
                    runtimeResponse = (HttpRuntimeResponse) runtimeService.handleRequest(httpRequest).get(0);
                    runtimeConsole = runtimeService.getConsole();
                }

                //now execution has finished, we can apply delays as configured
                long calculatedDelayForRoute = calculateResponseDelay(runtimeResponse, routeMatch, requestTimer);

                mapResponse(response, runtimeResponse, runtimeRequest, requestTimer);

                //if we have a delay of some kind, apply it.
                if(calculatedDelayForRoute > 0){
                    doneAsync = true;
                    delayedRequests.put(new Long(System.currentTimeMillis()+calculatedDelayForRoute), asyncContext);
                }

                logConsole(runtimeConsole);
                logResponse(runtimeResponse);

            } catch (Exception e) {
                logger.error("Error processing request", e);

                //write out error as json
                response.setStatus(500);
                response.setHeader("Content-Type","application/json");
                try {
                    response.getWriter().write(convertExceptionToResponse(e));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            //return if the task will be completed async
            return doneAsync;

        });

        runningRequests.put(task, asyncContext);
        engineExecutor.submit(task);

    }

    private RuntimeService getRuntimeService() throws Exception {
        return (RuntimeService) serviceManager.getService(SANDBOX_ID, Thread.currentThread().getName());
    }

    //synchronize on routing table creation so we only do it once, should only be done on first req / definition change anyway
    private synchronized RoutingTable getOrCreateRoutingTable() throws Exception {
        RoutingTable routingTable = cache.getRoutingTableForSandboxId(SANDBOX_ID, SANDBOX_ID);
        if(routingTable == null) {
            //create the routing table
            routingTable = getRuntimeService().handleRoutingTableRequest();
            //enrich with given runtime config (if any)
            addRouteConfigToRoutingTable(routingTable, config);
            //cache the routing table for use until it changes..
            cache.setRoutingTableForSandboxId(SANDBOX_ID, SANDBOX_ID, routingTable);
        }
        return routingTable;
    }

    private void logRequest(HttpRuntimeRequest request, HTTPRoute matchedRouteDetails){
        if(config.isDisableLogging()) return;

        String matchedRouteDescription = "No matching route";
        if(matchedRouteDetails != null) matchedRouteDescription = "Matched route '" + matchedRouteDetails.getPath() + "'";

        String bodyDescription = "No body found";
        if(StringUtils.hasLength(request.getBody())) {
            String truncatedBody = renderBody(request.getBody(), request.getHeaders());
            bodyDescription = "Body: '" + truncatedBody + "' (" + truncatedBody.length() + " bytes)";
        }

        logger.info("\n>> HTTP {} {}  ({})\n" +
                    ">> Headers: {}\n" +
                    ">> {}",
                request.getMethod(), request.getRawUrl(), matchedRouteDescription, getSafe(request.getHeaders(), new HashMap<>()), bodyDescription);

    }

    private void logConsole(Console console){
        if(config.isDisableLogging() || console == null) return;

        for (String logItem : console.getMessages()){
            String trimmedLogItem = logItem.trim();
            if(trimmedLogItem.endsWith("\n")) trimmedLogItem = trimmedLogItem.substring(0, trimmedLogItem.length()-2);
            logger.info(trimmedLogItem);

            if(config.getMetadataPort() != null){
                ActivityMessage activityMessage = new ActivityMessage(
                    SANDBOX_ID,
                    ActivityMessageTypeEnum.log,
                    logItem
                );
                activityMessage.setMessageId(messageIdCounter.getAndIncrement() + "");
                activityStore.add(activityMessage);
            }
        }

    }

    private void logResponse(RuntimeResponse response){
        if(config.isDisableLogging()) return;

        //then response
        String bodyDescription = "No body found";
        if(StringUtils.hasLength(response.getBody())) {
            String truncatedBody = renderBody(response.getBody(), response.getHeaders());
            bodyDescription = "Body: '" + truncatedBody + "'";
        }

        if(response instanceof RuntimeResponse){
            logger.info("<< Status: {} (took {}ms)\n" +
                            "<< Headers: {}\n" +
                            "<< {}",
                    ((HttpRuntimeResponse)response).getStatusCode(), response.getDurationMillis(), getSafe(response.getHeaders(), new HashMap<>()), bodyDescription);
        }
    }

    private long calculateResponseDelay(RuntimeResponse runtimeResponse, HTTPRoute routeMatch, StopWatch requestTimer){
        //now execution has finished, we can apply delays as configured
        long calculatedDelayForRoute = 0;
        //if we have a programmatically set delay, use that.
        if(runtimeResponse.getResponseDelay() > 0){
            calculatedDelayForRoute = runtimeResponse.getResponseDelay();
        }

        //otherwise check route config
        if(calculatedDelayForRoute == 0){
            calculatedDelayForRoute = RouteConfigUtils.calculate(routeMatch.getRouteConfig(), requestTimer, new AtomicInteger(1));
        }

        return calculatedDelayForRoute;
    }

    private String renderBody(String body, Map<String, String> headers){

        if(config.isVerboseLogging()){
            if(formatUtils.isXml(headers)) return formatUtils.formatXml(body);
            return body;
        }else{
            body = body.replace('\n',' ');
            if(body.length() > 150){
                return body.substring(0,150)+"...";
            }else{
                return body;
            }
        }
    }

    private <T> T getSafe(T obj, T defaultValue){
        if(obj == null) return defaultValue;
        return obj;
    }

    //gets the matching route (if any) out of the routing table
    private HTTPRoute findMatchedRoute(HttpRuntimeRequest request, RoutingTable table) throws Exception {
        //if we have a SOAP Action header, assume this is SOAP, amaze? And go get the operation name
        if("xml".equals(request.getContentType())){
            try {
                if(request.getHeaders().get("SOAPAction") != null) request.getProperties().put("SOAPAction", request.getHeaders().get("SOAPAction"));

                String soapBodyXPath = "local-name(//*[local-name()='Envelope']/*[local-name()='Body']/*[1])";
                String operationName = new XMLDoc(request.getBody()).get(soapBodyXPath, XPathConstants.STRING, String.class);
                // add to both maps,
                request.getProperties().put("SOAPOperationName", operationName);
                logger.debug("Found SOAP Operation Name: {}", operationName);

            } catch (Exception e) {
                logger.error("Error retrieving SOAP Operation Name", e);
            }
        }

        HTTPRoute match = (HTTPRoute) table.findMatch(request);

        //if its an OPTIONS request and we have a matching non-OPTIONS route then generate a route match
        if(match == null && request instanceof HttpRuntimeRequest && "OPTIONS".equalsIgnoreCase(request.getMethod())
                && table.findMatch("all", request.getUrl(), request.getProperties()) != null) {

            //if no match, but request is an options call, create a options match and default response
            return new HTTPRoute(request.getMethod(), request.getUrl(), request.getProperties());
        }

        //otherwise its just not there, return null
        if(match == null){
            return null;
        }

        Map<String, String> flattenedPathParams = mapUtils.flattenMultiValue(match.extractPathParams(request.getUrl()), URITemplate.FINAL_MATCH_GROUP);
        request.setPath(match.getPath());
        request.setParams(flattenedPathParams);

        return match;
    }

    private void addRouteConfigToRoutingTable(RoutingTable routingTable, RuntimeConfig runtimeConfig){
        routingTable.getRouteDetails().stream().filter(route -> route instanceof HTTPRoute).forEach(route -> {
            HTTPRoute httpRoute = (HTTPRoute) route;
            route.setRouteConfig(
                runtimeConfig.getRoutes().stream()
                    .filter(rc -> httpRoute.isUncompiledMatch(rc.getMethod(), rc.getPath(), Collections.emptyMap()))
                    .findFirst()
                    .orElse(null)
            );
        });
    }

    //wraps the exception message to mimic the standard sandbox proxy response
    private String convertExceptionMessageToResponse(String message){
        ObjectNode errorWrapper = mapper.createObjectNode();

        ObjectNode error = mapper.convertValue(new Error(message), ObjectNode.class);
        errorWrapper.put("errors", mapper.convertValue(Arrays.asList(error), ArrayNode.class));

        return errorWrapper.toString();
    }

    private String convertExceptionToResponse(Exception e){
        return convertExceptionMessageToResponse(e.getMessage());
    }

    //map a non-exception response, could be success or error
    private void mapResponse(HttpServletResponse response, RuntimeResponse runtimeResponse, HttpRuntimeRequest runtimeRequest, StopWatch requestTimer) throws Exception {

        //setup CORS headers
        runtimeResponse.getHeaders().put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        runtimeResponse.getHeaders().put("Access-Control-Allow-Origin", runtimeRequest.getHeaders().getOrDefault("Origin", "*"));
        runtimeResponse.getHeaders().put("Access-Control-Allow-Headers", runtimeRequest.getHeaders().getOrDefault("Access-Control-Request-Headers", "Content-Type"));
        runtimeResponse.getHeaders().put("Access-Control-Allow-Credentials", "true");
        //set processing time
        runtimeResponse.setDurationMillis(requestTimer.getTotalTimeMillis());

        //headers
        if (runtimeResponse.getHeaders() != null) {
            for (String key : runtimeResponse.getHeaders().keySet()) {
                response.setHeader(key, runtimeResponse.getHeaders().get(key));
            }
        }

        if(runtimeResponse instanceof HttpRuntimeResponse){
            HttpRuntimeResponse httpResponse = (HttpRuntimeResponse) runtimeResponse;
            //status
            int statusCode = httpResponse.getStatusCode() <= 0 ? 200 : httpResponse.getStatusCode();
            if(httpResponse.getStatusText() != null && response instanceof Response){
                ((Response)response).setStatusWithReason(statusCode, httpResponse.getStatusText());
            }else{
                response.setStatus(statusCode);
            }

            //cookies
            if (httpResponse.getCookies() != null) {
                for (String[] cookie : httpResponse.getCookies()) {
                    response.addHeader("Set-Cookie", cookie[0] + "=" + cookie[1]);
                }

            }
        }

        if(runtimeResponse.isError()){
            //write out the error
            response.setContentType("application/json");
            response.getWriter().append(mapper.writeValueAsString(runtimeResponse.getError()));

        }else{
            //write out the body
            response.getWriter().append(runtimeResponse.getBody());
        }

        if(config.getMetadataPort() != null) {
            ActivityMessage activityMessage = new ActivityMessage(
                SANDBOX_ID,
                ActivityMessageTypeEnum.request,
                mapper.writeValueAsString(new RuntimeTransaction(runtimeRequest, runtimeResponse))
            );
            activityMessage.setMessageId(messageIdCounter.getAndIncrement() + "");
            activityStore.add(activityMessage);
        }

    }
}
