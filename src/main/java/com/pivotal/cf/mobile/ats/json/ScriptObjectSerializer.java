/*
 * Copyright (c) 2011-2014 GoPivotal, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.pivotal.cf.mobile.ats.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import jdk.nashorn.internal.objects.NativeDate;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Will Tran <wtran@pivotallabs.com>
 */
public class ScriptObjectSerializer extends StdSerializer<ScriptObject> {
    public ScriptObjectSerializer() {
        super(ScriptObject.class);
    }

    private StdDelegatingSerializer arraySerializer = null;

    private StdDelegatingSerializer objectSerializer = null;

    private StdDelegatingSerializer dateSerializer = null;

    private StdDelegatingSerializer functionSerializer = null;

    @Override
    public void serialize(ScriptObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonGenerationException {

        if (value instanceof NativeDate) {
            if (dateSerializer == null) {
                dateSerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObject.class,
                        new StdConverter<ScriptObject, String>() {
                            @Override
                            public String convert(ScriptObject value) {
                                return NativeDate.toJSON(value, null).toString();
                            }
                        }).createContextual(provider, null);
            }
            dateSerializer.serialize(value, jgen, provider);
        } else if (value instanceof ScriptFunction) {
            if (functionSerializer == null) {
                functionSerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObject.class,
                        new StdConverter<ScriptObject, Object>() {
                            @Override
                            public Object convert(ScriptObject value) {
                                return null;
                            }
                        }).createContextual(provider, null);
            }
            functionSerializer.serialize(value, jgen, provider);
        } else if (value.isArray()) {
            if (arraySerializer == null) {
                arraySerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObject.class,
                        new StdConverter<ScriptObject, Collection<Object>>() {
                            @Override
                            public Collection<Object> convert(ScriptObject value) {
                                return value.values();
                            }
                        }).createContextual(provider, null);
            }
            arraySerializer.serialize(value, jgen, provider);
        } else {
            if (objectSerializer == null) {
                objectSerializer = (StdDelegatingSerializer) new StdDelegatingSerializer(ScriptObject.class,
                        new StdConverter<ScriptObject, Map<String, Object>>() {
                            @Override
                            public Map<String, Object> convert(ScriptObject value) {
                                Map<String, Object> convertedMap = new LinkedHashMap<String, Object>();
                                value.propertyIterator().forEachRemaining(k -> convertedMap.put(k, value.get(k)));
                                return convertedMap;
                            }
                        }).createContextual(provider, null);
            }
            objectSerializer.serialize(value, jgen, provider);
        }
    }

}
