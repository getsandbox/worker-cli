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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.PathSegmentImpl;

public final class JAXRSUtils {

    private JAXRSUtils() {
    }

    public static List<PathSegment> getPathSegments(String thePath, boolean decode) {
        return getPathSegments(thePath, decode, true);
    }

    public static List<PathSegment> getPathSegments(String thePath, boolean decode,
                                                    boolean ignoreLastSlash) {
        List<PathSegment> theList =
                StringUtils.splitAsStream(thePath, "/")
                        .filter(StringUtils.notEmpty())
                        .map(p -> new PathSegmentImpl(p, decode))
                        .collect(Collectors.toList());

        int len = thePath.length();
        if (len > 0 && thePath.charAt(len - 1) == '/') {
            String value = ignoreLastSlash ? "" : "/";
            theList.add(new PathSegmentImpl(value, false));
        }
        return theList;
    }

    public static MultivaluedMap<String, String> getMatrixParams(String path, boolean decode) {
        int index = path.indexOf(';');
        return index == -1 ? new MetadataMap<String, String>()
                : JAXRSUtils.getStructuredParams(path.substring(index + 1), ";", decode, false);
    }

    /**
     * Retrieve map of query parameters from the passed in message
     * @return a Map of query parameters.
     */
    public static MultivaluedMap<String, String> getStructuredParams(String query,
                                                                     String sep,
                                                                     boolean decode,
                                                                     boolean decodePlus) {
        MultivaluedMap<String, String> map =
                new MetadataMap<String, String>(new LinkedHashMap<String, List<String>>());

        getStructuredParams(map, query, sep, decode, decodePlus);

        return map;
    }

    public static void getStructuredParams(MultivaluedMap<String, String> queries,
                                           String query,
                                           String sep,
                                           boolean decode,
                                           boolean decodePlus) {
        getStructuredParams(queries, query, sep, decode, decodePlus, false);
    }

    public static void getStructuredParams(MultivaluedMap<String, String> queries,
                                           String query,
                                           String sep,
                                           boolean decode,
                                           boolean decodePlus,
                                           boolean valueIsCollection) {
        if (!StringUtils.isEmpty(query)) {
            for (String part : query.split(sep)) { // fastpath expected
                int index = part.indexOf('=');
                final String name;
                String value = null;
                if (index == -1) {
                    name = part;
                } else {
                    name = part.substring(0, index);
                    value = index < part.length() ? part.substring(index + 1) : "";
                }
                if (valueIsCollection) {
                    for (String s : value.split(",")) {
                        addStructuredPartToMap(queries, sep, name, s, decode, decodePlus);
                    }
                } else {
                    addStructuredPartToMap(queries, sep, name, value, decode, decodePlus);
                }
            }
        }
    }

    private static void addStructuredPartToMap(MultivaluedMap<String, String> queries,
                                               String sep,
                                               String name,
                                               String value,
                                               boolean decode,
                                               boolean decodePlus) {

        if (value != null) {
            if (decodePlus && value.contains("+")) {
                value = value.replace('+', ' ');
            }
            if (decode) {
                value = (";".equals(sep))
                        ? HttpUtils.pathDecode(value) : HttpUtils.urlDecode(value);
            }
        }

        queries.add(HttpUtils.urlDecode(name), value);
    }

}
