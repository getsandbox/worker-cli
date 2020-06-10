package com.sandbox.worker.core.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.worker.MapUtils;
import com.sandbox.worker.core.js.models.BodyContentType;
import com.sandbox.worker.core.server.exceptions.ConverterException;
import com.sandbox.worker.models.HttpRuntimeRequest;
import com.sandbox.worker.models.HttpRuntimeResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class HttpMessageConverter {

    private static Pattern jsonPattern = Pattern.compile("^application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]*\\+)?json.*$", Pattern.CASE_INSENSITIVE);
    private static Pattern xmlPattern = Pattern.compile("(text\\/xml|application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]+\\+)?xml).*$", Pattern.CASE_INSENSITIVE);

    private static final MapUtils mapUtils = new MapUtils();
    private static final Logger LOG = LoggerFactory.getLogger(HttpMessageConverter.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    //This is split to allow CLI to default values in runtime request
    public HttpRuntimeRequest convertRequest(FullHttpRequest rawRequest, String remoteAddress) throws ConverterException {
        HttpRuntimeRequest request = new HttpRuntimeRequest();

        // use lb header to get the originating IP, take first IP if we have a comma separated list.
        if (!StringUtils.isEmpty(rawRequest.headers().get("X-Forwarded-For"))) {
            String realIp = rawRequest.headers().get("X-Forwarded-For");
            if (realIp.contains(",")) {
                request.setIp(rawRequest.headers().get("X-Forwarded-For").split(",")[0].trim());
            } else {
                request.setIp(rawRequest.headers().get("X-Forwarded-For").trim());
            }
        } else {
            request.setIp(remoteAddress.trim());
        }

        return processRequest(rawRequest, request);
    }

    public HttpRuntimeRequest processRequest(FullHttpRequest rawRequest, HttpRuntimeRequest request) throws ConverterException {

        //validate request before processing
        if (rawRequest == null) {
            throw new ConverterException("Bad request - please check that your request data is valid");
        }

        //start mapping values
        request.setSandboxName(extractSandboxName(rawRequest.headers()));
        request.setMethod(rawRequest.method().name());
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(rawRequest.uri());
        request.setRawQuery(queryStringDecoder.rawQuery());
        String decodedPath = queryStringDecoder.path();
        decodedPath = decodedPath.contains("?") ? decodedPath.substring(0, decodedPath.indexOf("?")) : decodedPath;
        request.setUrl(decodedPath);

        if (rawRequest.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
            String rawContentType = rawRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);

            if (jsonPattern.matcher(rawContentType).matches()) {
                request.setContentType(BodyContentType.JSON.getType());
            } else if (xmlPattern.matcher(rawContentType).matches()) {
                request.setContentType(BodyContentType.XML.getType());
            } else if (rawContentType.startsWith("application/x-www-form-urlencoded")) {
                request.setContentType(BodyContentType.URLENCODED.getType());
            } else if (rawContentType.startsWith("multipart/form-data")) {
                request.setContentType(BodyContentType.FORMDATA.getType());
                request.setContentParser(s -> {
                    Map<String, Object> map = new HashMap<>();
                    for (InterfaceHttpData p : new HttpPostRequestDecoder(rawRequest).getBodyHttpDatas()) {
                        map.put(p.getName(), ((HttpData) p).getString());
                    }
                    return ProxyObject.fromMap(map);
                });
            }
        }

        //content-length is within limit so process
        ByteBuf content = rawRequest.content();
        String rawBody = content.toString(Charset.defaultCharset());
        request.setBody(rawBody == null ? "" : rawBody);

        //parse parameter, query params, cookies, headers etc.
        Map<String, List<String>> queryParams = queryStringDecoder.parameters();
        request.setQuery(mapUtils.flattenMultiValue(queryParams));

        if (rawRequest.headers().contains(HttpHeaderNames.COOKIE)) {
            Map<String, String> cookies = new HashMap<>();
            Set<io.netty.handler.codec.http.cookie.Cookie> rawCookies = ServerCookieDecoder.LAX.decode(rawRequest.headers().get(HttpHeaderNames.COOKIE));
            if (rawCookies != null) {
                for (io.netty.handler.codec.http.cookie.Cookie servletCookie : rawCookies) {
                    cookies.put(servletCookie.name(), servletCookie.value());
                }
            }
            request.setCookies(cookies);
        }

        //normalise accepted header
        List<String> accepted = new ArrayList<String>();
        for(String acceptedValue : rawRequest.headers().getAll("Accept")){
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
        for(Map.Entry<String, String> entry : rawRequest.headers().entries()){
            //if the SOAPAction is wrapped in quotes, remove them to simplify matching.
            String value = entry.getValue();
            if(entry.getKey().equalsIgnoreCase("SOAPAction") && value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
            normalisedHeaders.put(entry.getKey(), value);
        }
        request.setHeaders(normalisedHeaders);
        return request;
    }

    public FullHttpResponse convertResponse(HttpRuntimeRequest runtimeRequest, HttpRuntimeResponse runtimeResponse) throws ConverterException {
        String responseBody;
        try {
            if(runtimeResponse.isError()){
                //write out the error
                responseBody = mapper.writeValueAsString(runtimeResponse.getError());
            }else{
                //write out the body
                responseBody = runtimeResponse.getBody();
                if (responseBody == null) {
                    responseBody = "";
                }
            }
        } catch (IOException e) {
            throw new ConverterException("Error processing response body", e);
        }

        FullHttpResponse result = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, wrappedBuffer(responseBody.getBytes()));

        //CORS
        Map<String, String> runtimeResponseHeaders = runtimeResponse.getHeaders();
        runtimeResponseHeaders.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        runtimeResponseHeaders.put("Access-Control-Allow-Origin", runtimeRequest.getHeaders().getOrDefault("Origin", "*"));
        runtimeResponseHeaders.put("Access-Control-Allow-Headers", runtimeRequest.getHeaders().getOrDefault("Access-Control-Request-Headers", "Content-Type"));
        runtimeResponseHeaders.put("Access-Control-Allow-Credentials", "true");

        //headers
        if (runtimeResponseHeaders != null) {
            for (String key : runtimeResponseHeaders.keySet()) {
                result.headers().set(key, runtimeResponseHeaders.get(key));
            }
        }

        //status
        int statusCode = runtimeResponse.getStatusCode() <= 0 ? 200 : runtimeResponse.getStatusCode();
        if(runtimeResponse.getStatusText() != null){
            result.setStatus(HttpResponseStatus.valueOf(statusCode, runtimeResponse.getStatusText()));
        }else{
            result.setStatus(HttpResponseStatus.valueOf(statusCode));
        }

        //cookies
        if (runtimeResponse.getCookies() != null) {
            for (String[] cookie : runtimeResponse.getCookies()) {
                result.headers().set("Set-Cookie", cookie[0] + "=" + cookie[1]);
            }
        }

        if(runtimeResponse.isError()) {
            //write out the error
            result.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            //body is already set
        }

        return result;
    }

    public String extractSandboxName(HttpHeaders rawHeaders){
        String host = rawHeaders.get("Host");
        if(host == null || !host.contains(".")) {
            return null;
        }
        // extract sandbox name from request hostname
        return host.substring(0, host.indexOf("."));
    }

}
