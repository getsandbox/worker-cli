package com.sandbox.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sandbox.runtime.converters.HttpServletConverter;
import com.sandbox.runtime.js.converters.HTTPRequestConverter;
import com.sandbox.runtime.js.services.RuntimeService;
import com.sandbox.runtime.models.Cache;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.HTTPRequest;
import com.sandbox.runtime.models.HttpRuntimeRequest;
import com.sandbox.runtime.models.HttpRuntimeResponse;
import com.sandbox.runtime.models.MatchedRouteDetails;
import com.sandbox.runtime.models.RoutingTable;
import com.sandbox.runtime.services.CommandLineProcessor;
import com.sandbox.runtime.utils.FormatUtils;
import com.sandbox.runtime.utils.MapUtils;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    Cache cache;

    @Autowired
    CommandLineProcessor commandLine;

    @Autowired
    private HttpServletConverter servletConverter;

    @Autowired
    private HTTPRequestConverter serviceConverter;

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    //handle is synchronized so that the JS processing is done on one thread.
    @Override
    public synchronized void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        baseRequest.setHandled(true);

        //defaulted
        String sandboxId = "1";
        String sandboxName = "name";
        try {
            long startedRequest = System.currentTimeMillis();
            String requestId = UUID.randomUUID().toString();

            //convert incoming request to InstanceHttpRequest
            HttpRuntimeRequest runtimeRequest = servletConverter.httpServletToInstanceHttpRequest(request);

            //get a runtime service instance
            RuntimeService runtimeService = context.getBean(RuntimeService.class);

            //create and lookup routing table
            RoutingTable routingTable = cache.getRoutingTableForSandboxId(sandboxId);
            if(routingTable == null) {
                routingTable = runtimeService.handleRoutingTableRequest(sandboxId);
                cache.setRoutingTableForSandboxId(sandboxId, routingTable);
            }
            MatchedRouteDetails routeMatch = findMatchedRoute(runtimeRequest, routingTable);
            //if no route match for given request, then log message and send error response.
            if(routeMatch == null){
                logger.warn("** Error processing request for {} {} - Invalid route", runtimeRequest.getMethod(), runtimeRequest.getPath() == null ? request.getRequestURI() : runtimeRequest.getPath());
                response.setStatus(500);
                response.setHeader("Content-Type","application/json");
                response.getWriter().write(convertExceptionMessageToResponse("Invalid route"));
                return;
            }

            //log request with route
            logRequest(runtimeRequest, routeMatch, requestId);

            //run request
            HTTPRequest httpRequest = serviceConverter.fromInstanceHttpRequest(runtimeService.getSandboxScriptEngine().getEngine(), runtimeRequest);
            HttpRuntimeResponse runtimeResponse = null;

            if("options".equalsIgnoreCase(httpRequest.getMethod())){
                //if options request, send back CORS headers
                runtimeResponse = new HttpRuntimeResponse("", 200, new HashMap<>(), new ArrayList<>());
                runtimeResponse.getHeaders().put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                runtimeResponse.getHeaders().put("Access-Control-Allow-Origin", runtimeRequest.getHeaders().getOrDefault("Origin", "*"));
                runtimeResponse.getHeaders().put("Access-Control-Allow-Headers", runtimeRequest.getHeaders().getOrDefault("Access-Control-Request-Headers", "Content-Type"));
                runtimeResponse.getHeaders().put("Access-Control-Allow-Credentials", "true");
            }else{
                //otherwise process normally
                runtimeResponse = runtimeService.handleRequest(sandboxId, sandboxId, httpRequest);
            }
            runtimeResponse.setDurationMillis(System.currentTimeMillis() - startedRequest);

            logConsole(runtimeService);

            logResponse(runtimeResponse, requestId);

            //set response data back onto servlet response
            mapResponse(runtimeResponse, response);

        } catch (Exception e) {
            logger.error("Error processing request", e);

            //write out error as json
            response.setStatus(500);
            response.setHeader("Content-Type","application/json");
            response.getWriter().write(convertExceptionToResponse(e));
        }
    }

    private void logRequest(HttpRuntimeRequest request, MatchedRouteDetails matchedRouteDetails, String requestId){
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

    private void logResponse(HttpRuntimeResponse response, String requestId){
        //then response
        String bodyDescription = "No body found";
        if(StringUtils.hasLength(response.getBody())) {
            String truncatedBody = renderBody(response.getBody(), response.getHeaders());
            bodyDescription = "Body: '" + truncatedBody + "'";
        }

        logger.info("<< Status: {} (took {}ms)\n" +
                "<< Headers: {}\n" +
                "<< {}",
                response.getStatusCode(), response.getDurationMillis(), getSafe(response.getHeaders(), new HashMap<>()), bodyDescription);
    }

    private String renderBody(String body, Map<String, String> headers){

        if(commandLine.isVerboseLogging()){
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
    private MatchedRouteDetails findMatchedRoute(HttpRuntimeRequest request, RoutingTable table) throws Exception {
        MatchedRouteDetails match = table.findMatch(request.getMethod(), request.getUrl(), request.getHeaders());
        if(match == null) return null;

        Map<String, String> flattenedPathParams = mapUtils.flattenMultiValue(match.getPathParams(), URITemplate.FINAL_MATCH_GROUP);
        request.setPath(match.getPath());
        request.setParams(flattenedPathParams);

        return match;
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
    private void mapResponse(HttpRuntimeResponse runtimeResponse, HttpServletResponse response) throws Exception {
        //status
        if (runtimeResponse.getStatusCode() <= 0) {
            response.setStatus(200);
        } else {
            response.setStatus(runtimeResponse.getStatusCode());
        }

        //headers
        if (runtimeResponse.getHeaders() != null) {
            for (String key : runtimeResponse.getHeaders().keySet()) {
                response.setHeader(key, runtimeResponse.getHeaders().get(key));
            }

        }

        //cookies
        if (runtimeResponse.getCookies() != null) {
            for (String[] cookie : runtimeResponse.getCookies()) {
                response.addHeader("Set-Cookie", cookie[0] + "=" + cookie[1]);
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