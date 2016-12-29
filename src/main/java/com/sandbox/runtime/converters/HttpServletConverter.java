package com.sandbox.runtime.converters;

import com.sandbox.runtime.models.http.HttpRuntimeRequest;
import com.sandbox.runtime.utils.MapUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickhoughton on 1/08/2014.
 */

public class HttpServletConverter extends RequestConverter{

    @Autowired
    MapUtils mapUtils;

    private static Logger logger = LoggerFactory.getLogger(HttpServletConverter.class);

    public HttpRuntimeRequest httpServletToInstanceHttpRequest(HttpServletRequest rawRequest) throws Exception {
        if(rawRequest == null) throw new Exception("Invalid route");

        HttpRuntimeRequest request = new HttpRuntimeRequest();

        //set temporary name, can overload to change it
        request.setSandboxName("sandbox");
        request.setMethod(rawRequest.getMethod());
        request.setUrl(rawRequest.getRequestURI());

        //use lb header
        String remoteIp = rawRequest.getHeader("X-Forwarded-For");
        request.setIp(remoteIp);

        if(rawRequest.getContentType() != null){
            String rawContentType = rawRequest.getContentType();

            if(jsonPattern.matcher(rawContentType).matches()){
                request.setContentType("json");
            }else if(xmlPattern.matcher(rawContentType).matches()){
                request.setContentType("xml");
            }else if(rawContentType.startsWith("application/x-www-form-urlencoded")){
                request.setContentType("urlencoded");
            }

        }

        request.setBody(IOUtils.toString(rawRequest.getInputStream()));

        Map queryParams = rawRequest.getParameterMap();
        request.setQuery(mapUtils.flattenMultiValue(queryParams));
        request.setRawQuery(rawRequest.getQueryString());

        Map<String, String> cookies = new HashMap<>();
        if(rawRequest.getCookies() != null) {
            for (javax.servlet.http.Cookie servletCookie : rawRequest.getCookies()) {
                cookies.put(servletCookie.getName(), servletCookie.getValue());
            }
        }
        request.setCookies(cookies);

        request.setAccepted(
                getAcceptedHeadersFromHeaders(rawRequest.getHeaders("Accept"))
        );

        request.setHeaders(
                getHeadersAsMap(rawRequest)
        );

        return request;
    }

    public MapUtils getMapUtils() {
        return mapUtils;
    }
}
