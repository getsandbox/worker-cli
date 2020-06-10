package com.sandbox.worker.core.js.models;


import com.sandbox.worker.core.exceptions.ServiceScriptException;
import com.sandbox.worker.core.utils.XMLDoc;
import com.sandbox.worker.models.interfaces.BodyParserFunction;

import javax.activation.MimetypesFileTypeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

@HostAccess.Implementable
public class WorkerHttpRequest {

    @HostAccess.Export
    public final ProxyObject headers;
    @HostAccess.Export
    public final ProxyObject properties;
    @HostAccess.Export
    public final Object body;
    @HostAccess.Export
    public final String contentType;
    @HostAccess.Export
    public final String ip;
    @HostAccess.Export
    public final XMLDoc xmlDoc;

    //the actual url being requested by the client '/blah/something'
    @HostAccess.Export
    public final String path;
    @HostAccess.Export
    public final String method;
    @HostAccess.Export
    public final ProxyObject query;
    @HostAccess.Export
    public final ProxyObject params;
    @HostAccess.Export
    public final ProxyObject cookies;
    @HostAccess.Export
    public final ProxyArray accepted;
    //the potentially templated/uncompiled path defined in the route, '/blah/something' or '/blah/{item}'
    @HostAccess.Export
    public final String url;

    private static MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    public WorkerHttpRequest(BodyParserFunction<String, Object> bodyParser, String path, String method, Map<String, String> headers,
                             Map<String, String> properties, Map<String, String> query, Map<String, String> params,
                             Map<String, String> cookies, String body, String contentType,
                             String ip, List<String> accepted, String url) throws ServiceScriptException {

        Map javaHeaders = headers != null ? headers : new HashMap<String, String>();
        this.headers = ProxyObject.fromMap(javaHeaders);
        this.properties = ProxyObject.fromMap(properties != null ? new HashMap(properties) : new HashMap<>());
        this.contentType = contentType != null ? contentType : "";
        this.ip = ip != null ? ip : "";

        Object parsedBody = null;

        // if the body is non-zero length then parse it
        if (body != null && !body.isEmpty()) {
            try {
                if (bodyParser != null) {
                    parsedBody = bodyParser.apply(body);

                } else {
                    parsedBody = body;
                }
            } catch (Exception e) {
                throw new ServiceScriptException("Can't parse body of type " + this.contentType, e);
            }
        }

        if(parsedBody instanceof XMLDoc) {
            this.body = null;
            this.xmlDoc = (XMLDoc) parsedBody;
        } else {
            this.body = parsedBody;
            this.xmlDoc = null;
        }

        // set default values
        this.path = path != null ? path : "";
        this.method = method != null ? method : "";
        Map javaQuery = query != null ? query : new HashMap<String, String>();
        this.query = ProxyObject.fromMap(javaQuery);
        Map javaParams = params != null ? params : new HashMap<String, String>();
        this.params = ProxyObject.fromMap(javaParams);
        Map javaCookies = cookies != null ? cookies : new HashMap<String, String>();
        this.cookies = ProxyObject.fromMap(javaCookies);
        List javaAccepted = accepted != null ? accepted : new ArrayList<>();
        this.accepted = ProxyArray.fromArray(javaAccepted);

        this.url = url != null ? url : "";
    }

    @HostAccess.Export
    public Object get(Object[] args) {
        if (getHeaders() == null) return null;
        String headerName = args[0].toString();
        //get lowercase key as should be case insensitive
        Object result = getHeaders().getMember(headerName.toLowerCase());
        if (result == null) return result;
        return result.toString();
    }

    @HostAccess.Export
    public boolean is(Object[] args){
        if (args.length == 1) {
            return is(args[0].toString());
        } else if (args.length >= 2){
            return is(args[0].toString(), args[1].toString());
        } else {
            return false;
        }
    }

    @HostAccess.Export
    public boolean is(String type) {
        return is(type, "Content-Type");
    }

    @HostAccess.Export
    public boolean is(String type, String header) {
        if (type == null || type.length() == 0) return false;
        if (headers == null || headers.getMember(header) == null) return false;

        String contentType;
        if (type.contains("/")) {
            contentType = type;
        } else if (!type.contains(".")) {
            contentType = mimeTypes.getContentType("." + type);
        } else {
            contentType = mimeTypes.getContentType(type);
        }

        return headers.getMember(header).toString().toLowerCase().startsWith(contentType.toLowerCase());
    }

    public ProxyObject getHeaders() {
        return headers;
    }

    public ProxyObject getProperties() {
        return properties;
    }

    public Object getBody() {
        return body;
    }

    @HostAccess.Export
    public String bodyAsString() {
        return body == null ? "" : body.toString();
    }

    public String getContentType() {
        return contentType;
    }

    @HostAccess.Export
    public String contentType() {
        return contentType;
    }

    public String getIp() {
        return ip;
    }

    public XMLDoc getXmlDoc() {
        return xmlDoc;
    }

    //using lowercase, non get prefixed method names so JS can find them when we do 'req.query.blah'.
    //Nashorn seems to inconsistently find the getQuery() method depending on inheritance?
    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public ProxyObject getQuery() {
        return query;
    }

    public ProxyObject getParams() {
        return params;
    }

    public ProxyObject getCookies() {
        return cookies;
    }

    public ProxyArray getAccepted() {
        return accepted;
    }

    public String getUrl() {
        return url;
    }

}