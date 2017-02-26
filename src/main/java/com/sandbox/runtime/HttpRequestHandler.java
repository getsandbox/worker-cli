package com.sandbox.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.runtime.converters.HttpServletConverter;
import com.sandbox.runtime.js.converters.HTTPRequestConverter;
import com.sandbox.runtime.js.services.RuntimeService;
import com.sandbox.runtime.js.services.ServiceManager;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.RoutingTable;
import com.sandbox.runtime.models.RoutingTableCache;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.XMLDoc;
import com.sandbox.runtime.models.config.RuntimeConfig;
import com.sandbox.runtime.models.http.HTTPRequest;
import com.sandbox.runtime.models.http.HTTPRoute;
import com.sandbox.runtime.models.http.HttpRuntimeRequest;
import com.sandbox.runtime.models.http.HttpRuntimeResponse;
import com.sandbox.runtime.services.RouteConfigUtils;
import com.sandbox.runtime.utils.FormatUtils;
import com.sandbox.runtime.utils.MapUtils;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nickhoughton on 18/10/2014.
 */
@Component
@Lazy
public class HttpRequestHandler extends AbstractHandler {

    @Autowired
    ApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MapUtils mapUtils;

    @Autowired
    FormatUtils formatUtils;

    @Autowired
    RoutingTableCache routingTableCache;

    @Autowired
    RuntimeConfig config;

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    private HttpServletConverter servletConverter;

    @Autowired
    private HTTPRequestConverter serviceConverter;

    private Map<Long, AsyncContext> delayedRequests = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    public HttpRequestHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (Long time : delayedRequests.keySet()){
                if(currentTime > time){
                    AsyncContext asyncContext = delayedRequests.remove(time);
                    asyncContext.complete();
                }
            }

        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    //handle is synchronized so that the JS processing is done on one thread.
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        baseRequest.setHandled(true);

        //defaulted
        String sandboxId = "1";
        String sandboxName = "name";
        try {
            StopWatch requestTimer = new StopWatch();
            requestTimer.start();

            String requestId = config.isDisableIDs() ? "1" : UUID.randomUUID().toString();

            //convert incoming request to InstanceHttpRequest
            HttpRuntimeRequest runtimeRequest = servletConverter.httpServletToInstanceHttpRequest(request);

            //get a runtime service instance
            RuntimeService runtimeService = (RuntimeService) serviceManager.getService(sandboxId, Thread.currentThread().getName());

            HttpRuntimeResponse runtimeResponse = null;

            //create and lookup routing table
            RoutingTable routingTable = routingTableCache.getRoutingTableForSandboxId(sandboxId, sandboxId);
            if(routingTable == null) {
                //create the routing table
                routingTable = runtimeService.handleRoutingTableRequest();
                //enrich with given runtime config (if any)
                addRouteConfigToRoutingTable(routingTable, config);
                //routingTableCache the routing table for use until it changes..
                routingTableCache.setRoutingTableForSandboxId(sandboxId, sandboxId, routingTable);
            }

            HTTPRoute routeMatch = findMatchedRoute(runtimeRequest, routingTable);

            if(routeMatch == null &&
                    runtimeRequest instanceof HttpRuntimeRequest &&
                    runtimeRequest.getMethod().equalsIgnoreCase("OPTIONS") &&
                    routingTable.findMatch("all", runtimeRequest.getUrl(), runtimeRequest.getProperties()) != null){

                runtimeResponse = new HttpRuntimeResponse("", 200, null, new HashMap<>(), new ArrayList<>());

            }else if(routeMatch == null){
                //if no route match for given request, then log message and send error response.
                logger.warn("** Error processing request for {} {} - Invalid route", runtimeRequest.getMethod(), runtimeRequest.getPath() == null ? request.getRequestURI() : runtimeRequest.getPath());
                response.setStatus(500);
                response.setHeader("Content-Type","application/json");
                response.getWriter().write(convertExceptionMessageToResponse("Invalid route"));
                return;
            }

            //log request with route
            if(!config.isDisableLogging()) logRequest(runtimeRequest, routeMatch, requestId);

            //run request
            HTTPRequest httpRequest = serviceConverter.fromInstanceHttpRequest(runtimeService.getSandboxScriptEngine().getEngine(), runtimeRequest);

            if("options".equalsIgnoreCase(httpRequest.method())){
                //if options request, send back CORS headers
                runtimeResponse = new HttpRuntimeResponse("", 200, null, new HashMap<>(), new ArrayList<>());
            }else{
                //otherwise process normally
                if(config.isEnableConcurrency()){
                    runtimeResponse = (HttpRuntimeResponse) runtimeService.handleRequest(httpRequest).get(0);
                }else{
                    //if concurrency disabled then synchronise JS execution
                    synchronized (this) {
                        runtimeResponse = (HttpRuntimeResponse) runtimeService.handleRequest(httpRequest).get(0);
                    }
                }
            }

            long calculatedDelayForRoute = RouteConfigUtils.calculate(routeMatch.getRouteConfig(), requestTimer, new AtomicInteger(1));

            if(calculatedDelayForRoute > 0){
                AsyncContext asyncContext = request.startAsync();
                mapResponse((HttpServletResponse) asyncContext.getResponse(), runtimeResponse, runtimeRequest, requestTimer);
                delayedRequests.put(new Long(System.currentTimeMillis()+calculatedDelayForRoute), asyncContext);

            }else{
                mapResponse(response, runtimeResponse, runtimeRequest, requestTimer);
            }

            if(!config.isDisableLogging()){
                logConsole(runtimeService);
                logResponse(runtimeResponse, requestId);
            }

        } catch (Exception e) {
            logger.error("Error processing request", e);

            //write out error as json
            response.setStatus(500);
            response.setHeader("Content-Type","application/json");
            response.getWriter().write(convertExceptionToResponse(e));
        }
    }

    private void logRequest(HttpRuntimeRequest request, HTTPRoute matchedRouteDetails, String requestId){
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

    private void logConsole(RuntimeService service){
        for (String logItem : service.getConsole()._getMessages()){
            String trimmedLogItem = logItem.trim();
            if(trimmedLogItem.endsWith("\n")) trimmedLogItem = trimmedLogItem.substring(0, trimmedLogItem.length()-2);
            logger.info(trimmedLogItem);
        }

    }

    private void logResponse(RuntimeResponse response, String requestId){
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
        if(match == null) return null;

        Map<String, String> flattenedPathParams = mapUtils.flattenMultiValue(match.getPathParams(), URITemplate.FINAL_MATCH_GROUP);
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

    }
}
