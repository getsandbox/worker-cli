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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import jdk.nashorn.internal.runtime.Undefined;

/**
 * @author Will Tran <wtran@pivotallabs.com>
 */
public class UndefinedSerializer extends StdDelegatingSerializer {

    public UndefinedSerializer() {
        super(Undefined.class, new StdConverter<Undefined, Object>() {
            @Override
            public Object convert(Undefined value) {
                return null;
            }
        });
    }

    public UndefinedSerializer(Converter<Object, ?> converter, JavaType delegateType,
            JsonSerializer<?> delegateSerializer) {
        super(converter, delegateType, delegateSerializer);
    }

    @Override
    protected StdDelegatingSerializer withDelegate(Converter<Object, ?> converter, JavaType delegateType,
            JsonSerializer<?> delegateSerializer) {
        return new UndefinedSerializer(converter, delegateType, delegateSerializer);
    }


}
