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
    Map<String, String> properties;
    ScriptSource defineSource;

    //the type of define function call, can be define() or soap()
    String defineType;
    ScriptSource functionSource;

    @JsonIgnore
    ExactMatchURITemplate uriTemplate;

    public RouteDetails() {

    }

    public RouteDetails(String method, String path, Map<String, String> properties) {

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
        this.properties = properties;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getMethod() {
        return method == null ? "" : method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getProperties() {
        if(properties == null) properties = new HashMap<>();
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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

    @JsonIgnore
    public boolean isWildcardMethod(String method){
        return method != null && (method.equals("*") || method.equalsIgnoreCase("all"));
    }

    @JsonIgnore
    public boolean isWildcardMethod(){
        return isWildcardMethod(this.method);
    }

    public boolean matchesMethod(String method){
        if(method == null || this.method == null) return false;

        if(isWildcardMethod(method)) {
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

    public boolean isMatch(RouteDetails otherRoute) {
        return isMatch(otherRoute.getMethod(), otherRoute.getPath(), otherRoute.getProperties());
    }

    public boolean isMatch(String method, String url, Map<String, String> properties) {
        //bit crap but match needs a map to store processed path params.
        MultivaluedMap<String, String> urlParams = new MultivaluedHashMap<>();
        return isMatch(method, url, urlParams, properties);
    }

    //matches based on actual url /blah/1 -> /blah/{smth}
    public boolean isMatch(String method, String url, MultivaluedMap urlParams, Map<String, String> properties){

        //if method isnt right, skip!
        if(!matchesMethod(method)) return false;
        //if headers arent right, skip!
        if(!matchesProperties(properties)) return false;

        //if paths are exactly the same then match
        if (getPath().equals(url)) return true;

        //method matches, so continue..
        ExactMatchURITemplate template = process();

        //if we have a match, then set it as the best match, because we could match more than one, we want the BEST match.. which i think should be the one with the shortest 'finalMatchGroup'..
        if(template.match(url, urlParams)) {
            return true;
        }else{
            return false;
        }
    }

    //matches based on uncompiled path /blah/{smth}
    public boolean equals(HTTPRequest req){
        return matchesMethod(req.getMethod()) && req.getPath().equalsIgnoreCase(path) && matchesProperties(req.getProperties());

    }
}
