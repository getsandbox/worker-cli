package com.sandbox.worker.core.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.MapUtils;
import com.sandbox.worker.core.server.exceptions.ConverterException;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMessageConverter {

    private static Pattern jsonPattern = Pattern.compile("^application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]*\\+)?json.*$", Pattern.CASE_INSENSITIVE);
    private static Pattern xmlPattern = Pattern.compile("(text\\/xml|application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]+\\+)?xml).*$", Pattern.CASE_INSENSITIVE);

    private static final MapUtils mapUtils = new MapUtils();
    private static final Logger LOG = LoggerFactory.getLogger(HttpMessageConverter.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private int maxContentLengthBytes = 1048576;

    public HttpMessageConverter() {
    }

    public HttpMessageConverter(int maxContentLengthBytes) {
        this.maxContentLengthBytes = maxContentLengthBytes;
    }

    public HttpRuntimeRequest convertRequest(HttpRequest rawRequest) throws ConverterException {
        HttpRuntimeRequest request = new HttpRuntimeRequest();
        return processRequest(rawRequest, request);
    }

    public HttpRuntimeRequest processRequest(HttpRequest rawRequest, HttpRuntimeRequest request) throws ConverterException {

        //validate request before processing
        if (rawRequest == null || request == null) {
            throw new ConverterException("Bad request - please check that your request data is valid");
        }

        //check content-length header to reject large messages
        if (rawRequest.getContentLength() > maxContentLengthBytes) {
            throw new ConverterException("Bad request - your request body exceeds the maximum allowed of " + maxContentLengthBytes + " bytes");
        }

        //start mapping values
        request.setSandboxName(extractSandboxName(rawRequest));
        request.setMethod(rawRequest.getMethod().name());
        request.setUrl(rawRequest.getUri().getPath());

        // use lb header to get the originating IP, take first IP if we have a comma separated list.
        if (!StringUtils.isEmpty(rawRequest.getHeaders().get("X-Forwarded-For"))) {
            String realIp = rawRequest.getHeaders().get("X-Forwarded-For");
            if (realIp.contains(",")) {
                request.setIp(rawRequest.getHeaders().get("X-Forwarded-For").split(",")[0].trim());
            } else {
                request.setIp(rawRequest.getHeaders().get("X-Forwarded-For").trim());
            }
        } else {
            request.setIp(rawRequest.getRemoteAddress().getAddress().getHostAddress().trim());
        }

        if (rawRequest.getContentType().isPresent()) {
            String rawContentType = ((MediaType) rawRequest.getContentType().get()).toString();

            if (jsonPattern.matcher(rawContentType).matches()) {
                request.setContentType("json");
            } else if (xmlPattern.matcher(rawContentType).matches()) {
                request.setContentType("xml");
            } else if (rawContentType.startsWith("application/x-www-form-urlencoded")) {
                request.setContentType("urlencoded");
            }
        }

        //content-length is within limit so process
        request.setBody((String) rawRequest.getBody(String.class).orElse(""));

        //check body length again, could be sent without a content-length..
        if (request.getBody().length() > maxContentLengthBytes) {
            throw new ConverterException("Bad request - your request body exceeds the maximum allowed of " + maxContentLengthBytes + " bytes");
        }

        //parse parameter, query params, cookies, headers etc.
        Map queryParams = rawRequest.getParameters().asMap();
        request.setQuery(mapUtils.flattenMultiValue(queryParams));

        Map<String, String> cookies = new HashMap<>();
        if (rawRequest.getCookies() != null) {
            for (Cookie servletCookie : rawRequest.getCookies().getAll()) {
                cookies.put(servletCookie.getName(), servletCookie.getValue());
            }
        }
        request.setCookies(cookies);

        //normalise accepted header
        List<String> accepted = new ArrayList<String>();
        for(String acceptedValue : rawRequest.getHeaders().getAll("Accept")){
            if(acceptedValue.indexOf(",")!=-1){
                for(String subValue : acceptedValue.split(",")){
                    accepted.add(subValue);
                }
            }else{
                accepted.add(acceptedValue);
            }
        }
        request.setAccepted(accepted);

        //normalise / map headers
        Map<String,String> normalisedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, List<String>> requestHeaders = rawRequest.getHeaders().asMap();
        for(String key : requestHeaders.keySet()){
            List<String> values = requestHeaders.get(key);
            for (String value : values){
                //if the SOAPAction is wrapped in quotes, remove them to simplify matching.
                if(key.equalsIgnoreCase("SOAPAction") && value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
                normalisedHeaders.put(key, value);
            }
        }
        request.setHeaders(normalisedHeaders);

        return request;
    }

    public HttpResponse convertResponse(HttpRuntimeRequest runtimeRequest, HttpRuntimeResponse runtimeResponse) throws ConverterException {
        MutableHttpResponse result = HttpResponse.ok();

        //CORS
        Map<String, String> runtimeResponseHeaders = runtimeResponse.getHeaders();
        runtimeResponseHeaders.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        runtimeResponseHeaders.put("Access-Control-Allow-Origin", runtimeRequest.getHeaders().getOrDefault("Origin", "*"));
        runtimeResponseHeaders.put("Access-Control-Allow-Headers", runtimeRequest.getHeaders().getOrDefault("Access-Control-Request-Headers", "Content-Type"));
        runtimeResponseHeaders.put("Access-Control-Allow-Credentials", "true");

        //headers
        if (runtimeResponseHeaders != null) {
            for (String key : runtimeResponseHeaders.keySet()) {
                result.header(key, runtimeResponseHeaders.get(key));
            }
        }

        //status
        int statusCode = runtimeResponse.getStatusCode() <= 0 ? 200 : runtimeResponse.getStatusCode();
        if(runtimeResponse.getStatusText() != null){
            result.status(statusCode, runtimeResponse.getStatusText());
        }else{
            result.status(statusCode);
        }

        //cookies
        if (runtimeResponse.getCookies() != null) {
            for (String[] cookie : runtimeResponse.getCookies()) {
                result.header("Set-Cookie", cookie[0] + "=" + cookie[1]);
            }
        }

        try {
            if(runtimeResponse.isError()){
                //write out the error
                result.contentType("application/json");
                result.body(mapper.writeValueAsString(runtimeResponse.getError()));
            }else{
                //write out the body
                result.body(runtimeResponse.getBody());
            }
        } catch (IOException e) {
            throw new ConverterException("Error processing response body", e);
        }

        return result;
    }

    public String extractSandboxName(HttpRequest rawRequest){
        String host = rawRequest.getHeaders().get("Host");
        if(host == null || !host.contains(".")) {
            return null;
        }
        // extract sandbox name from request hostname
        return host.substring(0, host.indexOf("."));
    }

}
