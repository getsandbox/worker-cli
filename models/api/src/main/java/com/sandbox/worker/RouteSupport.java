package com.sandbox.worker;

import com.sandbox.worker.models.DefaultHTTPRoute;
import com.sandbox.worker.models.interfaces.HTTPRoute;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.enums.LatencyStrategyEnum;
import com.sandbox.worker.models.interfaces.Route;
import com.sandbox.worker.models.RouteConfig;
import com.sandbox.worker.models.interfaces.RoutingTable;
import com.sandbox.worker.models.RuntimeRequest;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import org.apache.cxf.custom.jaxrs.model.ExactMatchURITemplate;

public class RouteSupport {

    public static String generateRouteIdentifier(String transportType, Map<String, String> properties){
        return generateRouteIdentifier(transportType, "", "", properties);
    }

    public static String generateRouteIdentifier(String transportType, String method, String path, Map<String, String> properties){
        if(properties == null){
            properties = Collections.emptyMap();
        }

        CRC32 instance = new CRC32();
        instance.update(transportType.toLowerCase().getBytes());
        instance.update(method.toLowerCase().getBytes());
        instance.update(path.getBytes());
        instance.update(properties.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining()).getBytes()
        );
        return Long.toHexString(instance.getValue());
    }

    public static String sanitiseRoutePath(String path) {
        if (path.equals("*") || path.equals("/*")) {
            //replace wildcard with a JAXRS friendly syntax
            path = "/{route: .*}";

        } else if (path.contains(":") && !(path.contains("{") && path.contains("}"))) {
            //replace simple :param express routes with JAXRS {param} style ones.
            Matcher matcher = Pattern.compile(":([A-z0-9]+)").matcher(path);
            while (matcher.find()) {
                String variable = matcher.group(0);
                path = path.replaceAll(variable, "{" + variable.substring(1) + "}");
            }

        }
        return path;
    }

    public static String sanitiseRouteMethod(String method) {
        if (isWildcardMethod(method)) {
            return "ALL";
        } else {
            return method;
        }
    }

    public static boolean isWildcardMethod(String method) {
        return method != null && (method.equals("*") || method.equalsIgnoreCase("all"));
    }

    public static boolean methodsMatch(String method1, String method2) {
        if (method1 == null || method2 == null) return false;

        //check both sides of the comparison for wildcard method1s
        if (isWildcardMethod(method2) || isWildcardMethod(method1)) {

            return true;
        } else {
            return method2.equalsIgnoreCase(method1);
        }
    }

    public static MultivaluedMap<String, String> extractPathParams(ExactMatchURITemplate uriTemplate, String uri) {
        MultivaluedHashMap pathParams = new MultivaluedHashMap<>();
        uriTemplate.match(uri, pathParams);
        return pathParams;
    }

    public static boolean pathsExactMatch(String path1, String path2) {
        return path1.equals(path2);
    }

    public static boolean propertiesMatch(Map<String, String> routeProperties, Map<String, String> requestProperties) {
        if (routeProperties == null && requestProperties == null) return true;
        if (routeProperties == null) routeProperties = new HashMap<>();
        if (requestProperties == null) requestProperties = new HashMap<>();

        boolean match = true;
        for (Map.Entry<String, String> entry : routeProperties.entrySet()) {
            if (!requestProperties.getOrDefault(entry.getKey(), "").equalsIgnoreCase(entry.getValue().trim())) {
                match = false;
                break;
            }
        }
        return match;
    }

    public static ExactMatchURITemplate getTemplate(String path){
        return new ExactMatchURITemplate(path);
    }

    public static boolean isMatch(Route route, RuntimeRequest runtimeRequest) {
        if(route instanceof HTTPRoute){
            return isMatch((HTTPRoute)route, runtimeRequest);
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    public static boolean isMatch(HTTPRoute route, RuntimeRequest runtimeRequest) {
        if (runtimeRequest instanceof HttpRuntimeRequest) {
            HttpRuntimeRequest httpReq = (HttpRuntimeRequest) runtimeRequest;

            return isMatch(route, httpReq.getMethod(), httpReq.getUrl(), httpReq.getProperties());
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    public static boolean isMatch(Route route, Route otherRoute) {
        if(route instanceof HTTPRoute && otherRoute instanceof HTTPRoute){
            return isMatch((HTTPRoute)route, (HTTPRoute)otherRoute);
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    public static boolean isMatch(HTTPRoute route, HTTPRoute otherRoute) {
        return isMatch(route, otherRoute.getMethod(), otherRoute.getPath(), otherRoute.getProperties());
    }

    public static boolean isMatch(Route route, String requestMethod, String requestUrl, Map<String, String> requestProperties) {
        if(route instanceof HTTPRoute){
            return isMatch((HTTPRoute)route, requestMethod, requestUrl, requestProperties);
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    //matches based on actual url /blah/1 -> /blah/{smth}
    public static boolean isMatch(HTTPRoute route, String requestMethod, String requestUrl, Map<String, String> requestProperties) {
        if (requestMethod == null || requestUrl == null || route.getMethod() == null || route.getPath() == null) return false;

        //if method isnt right, skip!
        if (!methodsMatch(route.getMethod(), requestMethod)) return false;
        //if headers arent right, skip!
        if (!propertiesMatch(route.getProperties(), requestProperties)) return false;

        //if paths are exactly the same then match
        if (pathsExactMatch(route.getPath(), requestUrl)) return true;

        //no match so far, so continue..
        ExactMatchURITemplate template = getTemplate(route.getPath());

        //if we have a match, then set it as the best match, because we could match more than one, we want the BEST match.. which i think should be the one with the shortest 'finalMatchGroup'..
        if (template.match(requestUrl)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUncompiledMatch(HTTPRoute route, String requestMethod, String requestPath, Map<String, String> requestProperties) {
        return methodsMatch(route.getMethod(), requestMethod) && pathsExactMatch(route.getPath(), requestPath) && propertiesMatch(route.getProperties(), requestProperties);
    }

    public static HTTPRoute findMatchedRoute(HttpRuntimeRequest request, RoutingTable table) {
        HTTPRoute match = (HTTPRoute) table.findMatch(request);

        //if its an OPTIONS request and we have a matching non-OPTIONS route then generate a route match
        if (match == null && request instanceof HttpRuntimeRequest && "OPTIONS".equalsIgnoreCase(request.getMethod())
                && table.findMatch("all", request.getUrl(), request.getProperties()) != null) {

            //if no match, but request is an options call, create a options match and default response
            return new DefaultHTTPRoute(request.getMethod(), request.getUrl(), request.getProperties());
        }

        //otherwise its just not there, return null
        if (match == null) {
            return null;
        }

        Map<String, String> flattenedPathParams = MapUtils.flattenMultiValue(
                extractPathParams(getTemplate(match.getPath()), request.getUrl()), ExactMatchURITemplate.FINAL_MATCH_GROUP
        );
        request.setPath(match.getPath());
        request.setParams(flattenedPathParams);
        return match;
    }

    public static long calculateDelay(RouteConfig routeConfig, long startTime, AtomicInteger concurrentConsumers){
        long calculatedDelay = calculateDelay(routeConfig, concurrentConsumers);
        long adjustedDelay = calculatedDelay - (System.currentTimeMillis() - startTime);
        return adjustedDelay > 0L ? adjustedDelay : 0L;
    }

    public static long calculateDelay(RouteConfig routeConfig, AtomicInteger concurrentConsumers){
        if(routeConfig != null && routeConfig.getLatencyType() != null && routeConfig.getLatencyType() != LatencyStrategyEnum.NONE){

            if(routeConfig.getLatencyType() == LatencyStrategyEnum.CONSTANT){
                //constant latency is same for everything
                return routeConfig.getLatencyMs();
            }else if(routeConfig.getLatencyType() == LatencyStrategyEnum.LINEAR){
                //linear latency is calculated as, # concurrent consumers * (latency ms * latency multiplier)
                return concurrentConsumers.get() * (routeConfig.getLatencyMs() * routeConfig.getLatencyMultiplier());
            }
        }

        return 0L;
    }
}
