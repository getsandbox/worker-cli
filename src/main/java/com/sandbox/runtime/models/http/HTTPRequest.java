package com.sandbox.runtime.models.http;


import com.sandbox.runtime.js.converters.NashornConverter;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.EngineResponse;
import com.sandbox.runtime.models.Error;
import com.sandbox.runtime.models.RuntimeResponse;
import com.sandbox.runtime.models.ServiceScriptException;
import com.sandbox.runtime.models.XMLDoc;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class HTTPRequest extends EngineRequest{

    final String path;
    final String method;
    final ScriptObject query;
    final ScriptObject params;
    final ScriptObject cookies;
    final List<String> accepted;
    final String url;

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    public HTTPRequest(ScriptEngine scriptEngine, String path, String method, Map<String, String> headers,
                       Map<String, String> properties, Map<String, String> query, Map<String, String> params,
                       Map<String, String> cookies, Object body, String contentType,
                       String ip, List<String> accepted, String url) throws Exception {

        super(scriptEngine, headers, properties, body, contentType, ip);

        // set default values
        this.path = path != null ? path : "";
        this.method = method != null ? method : "";
        Map javaQuery= query != null ? query : new HashMap<String, String>();
        this.query = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaQuery);
        Map javaParams= params != null ? params : new HashMap<String, String>();
        this.params = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaParams);
        Map javaCookies= cookies != null ? cookies : new HashMap<String, String>();
        this.cookies = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaCookies);
        this.accepted = accepted != null ? accepted : new ArrayList<String>();
        this.url = url != null ? url : "";
    }

    public String get(String headerName){
        if(getHeaders() == null) return null;
        //get lowercase key as should be case insensitive
        return getHeaders().get(headerName.toLowerCase()).toString();
    }

    //using lowercase, non get prefixed method names so JS can find them when we do 'req.query.blah'.
    //Nashorn seems to inconsistently find the getQuery() method depending on inheritance?
    public String path() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public String method() {
        return method;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public ScriptObject headers() {
        return super.headers();
    }

    @Override
    public ScriptObject getHeaders() {
        return super.getHeaders();
    }

    public ScriptObject query() {
        return query;
    }

    public ScriptObject getQuery() {
        return query;
    }

    public ScriptObject params() {
        return params;
    }

    public ScriptObject getParams() {
        return params;
    }

    public ScriptObject cookies() {
        return cookies;
    }

    public ScriptObject getCookies() {
        return cookies;
    }

    public List<String> accepted() {
        return accepted;
    }

    public List<String> getAccepted() {
        return accepted;
    }

    public String url() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean is(String type) {
        return super.is(type, "Content-Type");
    }

    public Map<String, String> properties() {
        return super.getProperties();
    }

    public Object body() {
        return super.getBody();
    }

    public String contentType() {
        return super.getContentType();
    }

    public String ip() {
        return super.getIp();
    }

    public XMLDoc xmlDoc() {
        return super.getXmlDoc();
    }

    public String bodyAsString() {
        return super.getBodyAsString();
    }

    private static List<String> accessibleProperties = new ArrayList();
    public List<String> _getAccessibleProperties() {
        return super._getAccessibleProperties(accessibleProperties, HTTPRequest.class);
    }

    public Exception _getNoRouteDefinitionException(){
        return new ServiceScriptException("Could not find a route definition matching your requested route " + method() + " " + path());
    }

    public RuntimeResponse _getErrorResponse(Error error){
        return new HttpRuntimeResponse(error);
    }

    @Override
    public EngineResponse _getMatchingResponse() {
        return new HTTPResponse();
    }

}