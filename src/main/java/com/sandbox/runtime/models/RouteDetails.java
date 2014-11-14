package com.sandbox.runtime.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.cxf.jaxrs.model.ExactMatchURITemplate;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickhoughton on 3/08/2014.
 */
public class RouteDetails implements Serializable{

    private static final long serialVersionUID = 7262164955602223539L;
    String method;
    String path;
    String originalPath;

    @JsonIgnore
    ExactMatchURITemplate uriTemplate;

    public RouteDetails() {
    }

    public RouteDetails(String method, String path) {

        //save original
        this.originalPath = path;

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

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
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

    public boolean isMatch(RouteDetails otherRoute) {
        return isMatch(otherRoute.getMethod(), otherRoute.getPath());
    }

    public boolean isMatch(String method, String path) {
        //bit crap but match needs a map to store processed path params.
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        return isMatch(method, path, map);
    }

    public boolean isMatch(String method, String path, MultivaluedMap map){

        //if method isnt right, skip!
        if(!matchesMethod(method) ) return false;

        //method matches, so continue..
        ExactMatchURITemplate template = process();
        String routeLiterals = template.getLiteralChars();

        //if we have a match, then set it as the best match, because we could match more than one, we want the BEST match.. which i think should be the one with the shortest 'finalMatchGroup'..
        if( template.match(path, map) ) {
            return true;
        }else{
            return false;
        }
    }
}
