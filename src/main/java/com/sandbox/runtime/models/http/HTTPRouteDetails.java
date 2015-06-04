package com.sandbox.runtime.models.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.RouteDetails;
import com.sandbox.runtime.models.RuntimeRequest;
import org.apache.cxf.jaxrs.model.ExactMatchURITemplate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class HTTPRouteDetails extends RouteDetails {

    String method;
    String path;

    @JsonIgnore
    ExactMatchURITemplate uriTemplate;

    @JsonIgnore
    MultivaluedMap<String, String> pathParams;

    public HTTPRouteDetails() {
        super();
    }

    public HTTPRouteDetails(String method, String path, Map<String, String> properties) {
        super();

        if(path.equals("*") || path.equals("/*")){
            //replace wildcard with a JAXRS friendly syntax
            path = "/{route: .*}";

        } else if(path.contains(":") && !(path.contains("{") && path.contains("}")) ){
            //replace simple :param express routes with JAXRS {param} style ones.
            Matcher matcher = Pattern.compile(":([A-z0-9]+)").matcher(path);
            while (matcher.find()){
                String variable = matcher.group(0);
                path = path.replaceAll(variable, "{" + variable.substring(1) + "}");
            }

        }

        //coalesce varied wildcard method into one
        if(method.equalsIgnoreCase("all") || method.equalsIgnoreCase("*")) method = "ALL";

        this.method = method;
        this.path = path;
        setProperties(properties);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MultivaluedMap<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(MultivaluedMap<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public ExactMatchURITemplate getUriTemplate() {
        return uriTemplate;
    }

    protected void setUriTemplate(ExactMatchURITemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public ExactMatchURITemplate process(){
        if(uriTemplate != null) return uriTemplate;

        uriTemplate = new ExactMatchURITemplate(getPath());
        return uriTemplate;
    }

    @JsonIgnore
    public boolean isWildcardMethod(){
        return return method != null && ((method.equals("*") || method.equalsIgnoreCase("all"));
    }

    public boolean matchesMethod(String method){
        if(method == null || this.method == null) return false;

        if(isWildcardMethod()) {
            return true;
        }else{
            return this.method.equalsIgnoreCase(method);
        }
    }

    //match explicit properties
    public boolean matchesProperties(Map<String, String> properties){
        if(properties == null) properties = new HashMap<>();

        boolean match = true;
        for (Map.Entry<String, String> entry : getProperties().entrySet()){
            if(!properties.getOrDefault(entry.getKey(), "").equalsIgnoreCase(entry.getValue().trim())) {
                match = false;
                break;
            }
        }
        return match;
    }

    @JsonIgnore
    @Override
    public String getProcessingKey() {
        return process().getLiteralChars();
    }

    @JsonIgnore
    @Override
    public String getDisplayKey() {
        return getPath().concat(getMethod());
    }

    @Override
    public boolean matchesRuntimeRequest(RuntimeRequest runtimeRequest) {
        pathParams = new MultivaluedHashMap<>();

        if(runtimeRequest instanceof  HttpRuntimeRequest){
            HttpRuntimeRequest httpReq = (HttpRuntimeRequest) runtimeRequest;

            return isMatch(httpReq.getMethod(), httpReq.getUrl(), pathParams, httpReq.getProperties());
        }else{
            return false;
        }
    }

    public boolean matchesRoute(RouteDetails otherRoute) {
        if(otherRoute instanceof HTTPRouteDetails){
            HTTPRouteDetails otherHttpRoute = (HTTPRouteDetails) otherRoute;
            return isMatch(otherHttpRoute.getMethod(), otherHttpRoute.getPath(), otherRoute.getProperties());
        }else{
            return false;
        }
    }

    public boolean isMatch(String method, String url, Map<String, String> properties) {
        //bit crap but match needs a map to store processed path params.
        MultivaluedMap<String, String> urlParams = new MultivaluedHashMap<>();
        return isMatch(method, url, urlParams, properties);
    }

    //matches based on actual url /blah/1 -> /blah/{smth}
    private boolean isMatch(String method, String url, MultivaluedMap urlParams, Map<String, String> properties){
        if(method == null || url == null || getMethod() == null || getPath() == null) return false;

        //if method isnt right, skip!
        if(!matchesMethod(method)) return false;
        //if headers arent right, skip!
        if(!matchesProperties(properties)) return false;

        //method matches, so continue..
        ExactMatchURITemplate template = process();

        //if paths are exactly the same then match
        if(getPath().equals(url)) return true;

        //if we have a match, then set it as the best match, because we could match more than one, we want the BEST match.. which i think should be the one with the shortest 'finalMatchGroup'..
        if(template.match(url, urlParams)) {
            return true;
        }else{
            return false;
        }
    }


    //matches based on uncompiled path /blah/{smth}
    public boolean matchesEngineRequest(EngineRequest req){
        if(req instanceof HTTPRequest){
            HTTPRequest httpReq = (HTTPRequest) req;
            return matchesMethod(httpReq.method()) && httpReq.path().equalsIgnoreCase(path) && matchesProperties(req.getProperties());
        }else{
            return false;
        }
    }
}
