package com.sandbox.worker.core.utils;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLEncodedUtils {

    public static HashMap<String, String> decodeBody(String body) {
        HashMap<String, String> queryMap = new HashMap<>();

        try {
            Map<String, Object> params = URISupport.parseQuery(body);
            for (String key : params.keySet()) {
                Object value = params.get(key);
                if (value instanceof String) {
                    value = URLDecoder.decode((String) value, "UTF-8");
                    queryMap.put(key, (String) value);
                }
                if (value instanceof List) {
                    for (Object listValue : (List) value) {
                        listValue = URLDecoder.decode((String) listValue, "UTF-8");
                        queryMap.put(key, (String) listValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse urlencoded body");
        }

        return queryMap;
    }
}
