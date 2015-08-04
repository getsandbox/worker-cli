package com.sandbox.runtime.js.serializers;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import jdk.nashorn.internal.runtime.Undefined;

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

