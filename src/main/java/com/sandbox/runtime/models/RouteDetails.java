package com.sandbox.runtime.models;

import org.apache.cxf.jaxrs.model.ExactMatchURITemplate;

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

    ExactMatchURITemplate uriTemplate;

    public RouteDetails() {
    }

    public RouteDetails(String method, String path) {

        //save original
        this.originalPath = path;

        if(path.equals("*") || path.equals("/*")){
            //replace wildcard with a JAXRS friendly syntax
            path = "/{route}";

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
        if(isWildcardMethod()){
            return true;
        }else{
            return this.method.equalsIgnoreCase(method);
        }
    }
}
