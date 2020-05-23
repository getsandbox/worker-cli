/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.custom.jaxrs.utils;

import javax.ws.rs.core.PathSegment;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cxf.custom.common.util.UrlUtils;
import org.apache.cxf.jaxrs.impl.PathSegmentImpl;

public final class HttpUtils {

    private static final Pattern ENCODE_PATTERN = Pattern.compile("%[0-9a-fA-F][0-9a-fA-F]");
    // there are more of such characters, ex, '*' but '*' is not affected by UrlEncode
    private static final String PATH_RESERVED_CHARACTERS = "=@/:!$&\'(),;~";

    private static final String QUERY_RESERVED_CHARACTERS = "?/,";

    private HttpUtils() {
    }

    public static String urlDecode(String value, String enc) {
        return UrlUtils.urlDecode(value, enc);
    }

    public static String urlDecode(String value) {
        return UrlUtils.urlDecode(value);
    }

    public static String pathDecode(String value) {
        return UrlUtils.pathDecode(value);
    }

    private static String componentEncode(String reservedChars, String value) {

        StringBuilder buffer = new StringBuilder();
        StringBuilder bufferToEncode = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char currentChar = value.charAt(i);
            if (reservedChars.indexOf(currentChar) != -1) {
                if (bufferToEncode.length() > 0) {
                    buffer.append(urlEncode(bufferToEncode.toString()));
                    bufferToEncode.setLength(0);
                }
                buffer.append(currentChar);
            } else {
                bufferToEncode.append(currentChar);
            }
        }

        if (bufferToEncode.length() > 0) {
            buffer.append(urlEncode(bufferToEncode.toString()));
        }

        return buffer.toString();
    }

    public static String queryEncode(String value) {

        return componentEncode(QUERY_RESERVED_CHARACTERS, value);
    }

    public static String urlEncode(String value) {

        return urlEncode(value, StandardCharsets.UTF_8.name());
    }

    public static String urlEncode(String value, String enc) {

        return UrlUtils.urlEncode(value, enc);
    }

    public static String pathEncode(String value) {

        String result = componentEncode(PATH_RESERVED_CHARACTERS, value);
        // URLEncoder will encode '+' to %2B but will turn ' ' into '+'
        // We need to retain '+' and encode ' ' as %20
        if (result.indexOf('+') != -1) {
            result = result.replace("+", "%20");
        }
        if (result.indexOf("%2B") != -1) {
            result = result.replace("%2B", "+");
        }

        return result;
    }

    /**
     * Encodes partially encoded string. Encode all values but those matching pattern
     * "percent char followed by two hexadecimal digits".
     *
     * @param encoded fully or partially encoded string.
     * @return fully encoded string
     */
    public static String encodePartiallyEncoded(String encoded, boolean query) {
        if (encoded.length() == 0) {
            return encoded;
        }
        Matcher m = ENCODE_PATTERN.matcher(encoded);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (m.find()) {
            String before = encoded.substring(i, m.start());
            sb.append(query ? HttpUtils.queryEncode(before) : HttpUtils.pathEncode(before));
            sb.append(m.group());
            i = m.end();
        }
        String tail = encoded.substring(i, encoded.length());
        sb.append(query ? HttpUtils.queryEncode(tail) : HttpUtils.pathEncode(tail));
        return sb.toString();
    }

    public static String fromPathSegment(PathSegment ps) {
        if (PathSegmentImpl.class.isAssignableFrom(ps.getClass())) {
            return ((PathSegmentImpl)ps).getOriginalPath();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ps.getPath());
        for (Map.Entry<String, List<String>> entry : ps.getMatrixParameters().entrySet()) {
            for (String value : entry.getValue()) {
                sb.append(';').append(entry.getKey());
                if (value != null) {
                    sb.append('=').append(value);
                }
            }
        }
        return sb.toString();
    }

}
