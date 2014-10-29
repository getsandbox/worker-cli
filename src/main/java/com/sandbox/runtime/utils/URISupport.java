package com.sandbox.runtime.utils;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracted from Camel URISupport to break the dependency
 */
public class URISupport {

    public static final String RAW_TOKEN_START = "RAW(";
    public static final String RAW_TOKEN_END = ")";
    private static final String CHARSET = "UTF-8";

    /**
     * Parses the query part of the uri (eg the parameters).
     * <p/>
     * The URI parameters will by default be URI encoded. However you can define a parameter
     * values with the syntax: <tt>key=RAW(value)</tt> which tells Camel to not encode the value,
     * and use the value as is (eg key=value) and the value has <b>not</b> been encoded.
     *
     * @param uri the uri
     * @return the parameters, or an empty map if no parameters (eg never null)
     * @throws java.net.URISyntaxException is thrown if uri has invalid syntax.
     */
    public static Map<String, Object> parseQuery(String uri) throws URISyntaxException {
        return parseQuery(uri, false);
    }

    /**
     * Parses the query part of the uri (eg the parameters).
     * <p/>
     * The URI parameters will by default be URI encoded. However you can define a parameter
     * values with the syntax: <tt>key=RAW(value)</tt> which tells Camel to not encode the value,
     * and use the value as is (eg key=value) and the value has <b>not</b> been encoded.
     *
     * @param uri the uri
     * @param useRaw whether to force using raw values
     * @return the parameters, or an empty map if no parameters (eg never null)
     * @throws URISyntaxException is thrown if uri has invalid syntax.
     */
    public static Map<String, Object> parseQuery(String uri, boolean useRaw) throws URISyntaxException {
        // must check for trailing & as the uri.split("&") will ignore those
        if (uri != null && uri.endsWith("&")) {
            throw new URISyntaxException(uri, "Invalid uri syntax: Trailing & marker found. "
                    + "Check the uri and remove the trailing & marker.");
        }

        if (uri.isEmpty()) {
            // return an empty map
            return new LinkedHashMap<String, Object>(0);
        }

        // need to parse the uri query parameters manually as we cannot rely on splitting by &,
        // as & can be used in a parameter value as well.

        try {
            // use a linked map so the parameters is in the same order
            Map<String, Object> rc = new LinkedHashMap<String, Object>();

            boolean isKey = true;
            boolean isValue = false;
            boolean isRaw = false;
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();

            // parse the uri parameters char by char
            for (int i = 0; i < uri.length(); i++) {
                // current char
                char ch = uri.charAt(i);
                // look ahead of the next char
                char next;
                if (i <= uri.length() - 2) {
                    next = uri.charAt(i + 1);
                } else {
                    next = '\u0000';
                }

                // are we a raw value
                isRaw = value.toString().startsWith(RAW_TOKEN_START);

                // if we are in raw mode, then we keep adding until we hit the end marker
                if (isRaw) {
                    if (isKey) {
                        key.append(ch);
                    } else if (isValue) {
                        value.append(ch);
                    }

                    // we only end the raw marker if its )& or at the end of the value

                    boolean end = ch == RAW_TOKEN_END.charAt(0) && (next == '&' || next == '\u0000');
                    if (end) {
                        // raw value end, so add that as a parameter, and reset flags
                        addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
                        key.setLength(0);
                        value.setLength(0);
                        isKey = true;
                        isValue = false;
                        isRaw = false;
                        // skip to next as we are in raw mode and have already added the value
                        i++;
                    }
                    continue;
                }

                // if its a key and there is a = sign then the key ends and we are in value mode
                if (isKey && ch == '=') {
                    isKey = false;
                    isValue = true;
                    isRaw = false;
                    continue;
                }

                // the & denote parameter is ended
                if (ch == '&') {
                    // parameter is ended, as we hit & separator
                    addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
                    key.setLength(0);
                    value.setLength(0);
                    isKey = true;
                    isValue = false;
                    isRaw = false;
                    continue;
                }

                // regular char so add it to the key or value
                if (isKey) {
                    key.append(ch);
                } else if (isValue) {
                    value.append(ch);
                }
            }

            // any left over parameters, then add that
            if (key.length() > 0) {
                addParameter(key.toString(), value.toString(), rc, useRaw || isRaw);
            }

            return rc;

        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }

    private static void addParameter(String name, String value, Map<String, Object> map, boolean isRaw) throws UnsupportedEncodingException {
        name = URLDecoder.decode(name, CHARSET);
        if (!isRaw) {
            // need to replace % with %25
            value = URLDecoder.decode(value.replaceAll("%", "%25"), CHARSET);
        }

        // does the key already exist?
        if (map.containsKey(name)) {
            // yes it does, so make sure we can support multiple values, but using a list
            // to hold the multiple values
            Object existing = map.get(name);
            List<String> list;
            if (existing instanceof List) {
                list = (List) existing;
            } else {
                // create a new list to hold the multiple values
                list = new ArrayList<String>();
                String s = existing != null ? existing.toString() : null;
                if (s != null) {
                    list.add(s);
                }
            }
            list.add(value);
            map.put(name, list);
        } else {
            map.put(name, value);
        }
    }
}
