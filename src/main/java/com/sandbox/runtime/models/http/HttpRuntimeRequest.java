package com.sandbox.runtime.models.http;

import com.sandbox.runtime.models.RuntimeRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by nickhoughton on 1/08/2014.
 */
public class HttpRuntimeRequest extends RuntimeRequest {

    Map<String, String> cookies;
//    List<Cookie> signedCookies;

    Map<String, String> query;

    String rawQuery;

    List<String> accepted;

    String url;

    Map<String, String> params;

    String method;

    String path;

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public List<String> getAccepted() {
        return accepted;
    }

    public void setAccepted(List<String> accepted) {
        this.accepted = accepted;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRawUrl() {
        if(rawQuery == null || rawQuery.isEmpty()){
            return getUrl();
        }else{
            return getUrl() + "?" + getRawQuery();
        }
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
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

}
