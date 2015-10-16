package com.sandbox.runtime.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sandbox.runtime.js.converters.NashornConverter;
import com.sandbox.runtime.js.models.JsonNode;
import com.sandbox.runtime.utils.URISupport;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drew on 30/07/2014.
 */
public class HTTPRequest {

    final String path;
    final String method;
    final ScriptObject headers;
    final Map<String, String> headersMap;
    final Map<String, String> properties;
    final ScriptObject query;
    final ScriptObject params;
    final ScriptObject cookies;
    final Object body;
    final String contentType;
    final String ip;
    final List<String> accepted;
    final String url;
    final XMLDoc xmlDoc;

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    public HTTPRequest(ScriptEngine scriptEngine, String path, String method, Map<String, String> headers,
                       Map<String, String> properties, Map<String, String> query, Map<String, String> params,
                       Map<String, String> cookies, Object body, String contentType,
                       String ip, List<String> accepted, String url) throws Exception {

        // set default values
        this.path = path != null ? path : "";
        this.method = method != null ? method : "";
        Map javaHeaders = headers != null ? headers : new HashMap<String, String>();
        this.headers = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaHeaders);
        this.headersMap = javaHeaders;
        Map javaQuery= query != null ? query : new HashMap<String, String>();
        this.query = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaQuery);
        Map javaParams= params != null ? params : new HashMap<String, String>();
        this.params = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaParams);
        Map javaCookies= cookies != null ? cookies : new HashMap<String, String>();
        this.cookies = (ScriptObject) NashornConverter.instance().convert(scriptEngine, javaCookies);

        this.properties = properties != null ? properties : new HashMap<String, String>();
        this.contentType = contentType != null ? contentType : "";
        this.ip = ip != null ? ip : "";
        this.accepted = accepted != null ? accepted : new ArrayList<String>();
        this.url = url != null ? url : "";

        Object _body = null;
        XMLDoc _xmlDoc = null;

        // if the body is non-zero length then parse it
        if (body != null && !body.toString().isEmpty()) {
            try {
                if ("json".equalsIgnoreCase(this.contentType)) {
                    _body = new JsonNode((String) body).getJsonObject();
                    //convert java arrays/maps to JS ones
                    _body = NashornConverter.instance().convert(scriptEngine, _body);

                } else if ("xml".equalsIgnoreCase(this.contentType)) {
                    _xmlDoc = new XMLDoc(body);

                } else if ("urlencoded".equalsIgnoreCase(this.contentType)){
                    _body = decodeBody((String) body);

                } else {
                    _body = body;
                }
            } catch(Exception e) {
                throw new ServiceScriptException("Can't parse body of type " + this.contentType);
            }
        }

        this.body = _body;
        this.xmlDoc = _xmlDoc;
    }

    public String get(String headerName){
        if(getHeaders() == null) return null;
        //get lowercase key as should be case insensitive
        return getHeaders().get(headerName.toLowerCase()).toString();
    }

    public boolean is(String type){
        if(type == null || type.length() == 0) return false;
        if(headers == null || headers.get("Content-Type") == null) return false;

        String contentType;
        if (type.contains("/")) {
            contentType = type;
        } else if (!type.contains(".")) {
            contentType = mimeTypes.getContentType("." + type);
        } else {
            contentType = mimeTypes.getContentType(type);
        }

        return headers.get("Content-Type").toString().toLowerCase().startsWith(contentType.toLowerCase());
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public ScriptObject getHeaders() {
        return headers;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public ScriptObject getQuery() {
        return query;
    }

    public ScriptObject getParams() {
        return params;
    }

    public Map<String, String> getHeadersMap() {
      return headersMap;
   }

   public ScriptObject getCookies() {
        return cookies;
    }

    public Object getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getIp() {
        return ip;
    }

    public List<String> getAccepted() {
        return accepted;
    }

    public String getUrl() {
        return url;
    }

    @JsonIgnore
    public XMLDoc getXmlDoc() {
        return xmlDoc;
    }

    @JsonIgnore
    public XMLDoc newXmlDoc(String xml) {
        try {
            return new XMLDoc(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JsonIgnore
    public String getBodyAsString() { return body.toString(); }

    private HashMap<String, String> decodeBody(String body) throws Exception{
        HashMap<String, String> queryMap = new HashMap<>();

        try {
            Map<String, Object> params = URISupport.parseQuery(body);
            for (String key : params.keySet()) {
                Object value = params.get(key);
                if(value instanceof String) {
                    value = URLDecoder.decode((String) value, "UTF-8");
                    queryMap.put(key, (String) value);
                }
                if(value instanceof List){
                    for (Object listValue : (List)value){
                        listValue = URLDecoder.decode((String) listValue, "UTF-8");
                        queryMap.put(key, (String) listValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to parse urlencoded body");
        }

        return queryMap;
    }

    private static List<String> accessibleProperties = new ArrayList();
    public List<String> _getAccessibleProperties() {
        if(!accessibleProperties.isEmpty()) return accessibleProperties;

        for (Field field : this.getClass().getDeclaredFields()){
            if(!Modifier.isPrivate(field.getModifiers())) accessibleProperties.add(field.getName());
        }

        for (Method method: this.getClass().getDeclaredMethods()){
            if(Modifier.isPublic(method.getModifiers())) accessibleProperties.add(method.getName());
        }

        return accessibleProperties;
    }

    // TODO: Needs refactoring
    private void sendNotification(String url, String method, Map<String, String> headers, Map<String, String> body)
            throws IOException {
        Request request;
        if (method.equalsIgnoreCase("get")) {
            request = Request.Get(url);

            for (String key : headers.keySet()) {
                request.addHeader(key, headers.get(key));
            }
        } else {
            request = Request.Post(url);

            Form form = Form.form();
            for (String key : body.keySet()) {
                form.add(key, body.get(key));
            }

            for (String key : headers.keySet()) {
                request.addHeader(key, headers.get(key));
            }
            request.bodyForm(form.build());
        }

        request.connectTimeout(5000)
                .socketTimeout(5000)
                .execute();
    }

    // TODO: Needs refactoring
    public String sendPostNotification(String targetUrl) {
        try {
            // Set body
            Map<String, String> body = new HashMap<>();
            if (this.body instanceof HashMap) {
                for (Object key : ((HashMap) this.body).keySet()) {
                    body.put(key.toString(), ((HashMap) this.body).get(key)
                            .toString());
                }
            }

            // Set headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", this.headers.get("Content-Type")
                    .toString());
            headers.put("User-Agent", this.headers.get("Content-Type")
                    .toString());

            sendNotification(targetUrl, "post", headers, body);

            return "{ \"status\": \"OK\", \"message\": \"Request successfully executed!\" }";
        } catch (IOException e) {
            return "{ \"status\": \"ERROR\", \"message\": \"" + e.toString() + "\" }";
        }
    }
}