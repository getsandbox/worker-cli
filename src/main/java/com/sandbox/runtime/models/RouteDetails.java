package com.sandbox.runtime.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.cxf.jaxrs.model.ExactMatchURITemplate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class RouteDetails implements Serializable{

    private static final long serialVersionUID = 7262164955602223539L;

    //transport type, at the moment only going to be HTTP
    String transport;
    String method;
    String path;
    Map<String, String> headers;
    ScriptSource defineSource;

    //the type of define function call, can be define() or soap()
    String defineType;
    ScriptSource functionSource;

    @JsonIgnore
    ExactMatchURITemplate uriTemplate;

    public RouteDetails() {
    }

    public RouteDetails(String method, String path, Map<String, String> headers) {

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

        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public ExactMatchURITemplate getUriTemplate() {
        return uriTemplate;
    }

    protected void setUriTemplate(ExactMatchURITemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public ScriptSource getDefineSource() {
        return defineSource;
    }

    public void setDefineSource(ScriptSource defineSource) {
        this.defineSource = defineSource;
    }

    public String getDefineType() {
        return defineType;
    }

    public void setDefineType(String defineType) {
        this.defineType = defineType;
    }

    public ScriptSource getFunctionSource() {
        return functionSource;
    }

    public void setFunctionSource(ScriptSource functionSource) {
        this.functionSource = functionSource;
    }

    public ExactMatchURITemplate process(){
        if(uriTemplate != null) return uriTemplate;

        uriTemplate = new ExactMatchURITemplate(getPath());
        return uriTemplate;
    }

    public boolean isWildcardMethod(){
        return method.equals("*") || method.equalsIgnoreCase("all");
    }

    public boolean matchesMethod(String method){
        if(isWildcardMethod()) {
            return true;
        }else if(method.equalsIgnoreCase("options")){
            //always match options
            return true;
        }else{
            return this.method.equalsIgnoreCase(method);
        }
    }

    //match this routes headers again the given headers, doesn't matter if the given headers have extra
    public boolean matchesHeaders(Map<String, String> headers){
        if(headers == null) headers = new HashMap<>();

        boolean match = true;
        for (Map.Entry<String, String> entry : getHeaders().entrySet()){
            if(!headers.getOrDefault(entry.getKey(),"").equals(entry.getValue().trim())) {
                match = false;
                break;
            }
        }
        return match;
    }

    public boolean isMatch(RouteDetails otherRoute) {
        return isMatch(otherRoute.getMethod(), otherRoute.getPath(), otherRoute.getHeaders());
    }

    public boolean isMatch(String method, String path) {
        //bit crap but match needs a map to store processed path params.
        MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
        return isMatch(method, path, pathParams, null);
    }

    public boolean isMatch(String method, String url, Map<String, String> headers) {
        //bit crap but match needs a map to store processed path params.
        MultivaluedMap<String, String> urlParams = new MultivaluedHashMap<>();
        return isMatch(method, url, urlParams, headers);
    }

    //matches based on actual url /blah/1 -> /blah/{smth}
    public boolean isMatch(String method, String url, MultivaluedMap urlParams, Map<String, String> headers){

        //if method isnt right, skip!
        if(!matchesMethod(method)) return false;
        //if headers arent right, skip!
        if(!matchesHeaders(headers)) return false;

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
    public boolean equals(HTTPRequest req){
        return matchesMethod(req.getMethod()) && req.getPath().equalsIgnoreCase(path) && matchesHeaders(req.getHeaders());
    }
}
