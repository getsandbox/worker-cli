package com.sandbox.worker.core.graal;

import java.util.AbstractList;
import org.graalvm.polyglot.Value;

public abstract class ValueListWrapper extends AbstractList<Object> {

    public static ValueListWrapper fromValue(Value values) {
        return new ValueListWrapper() {
            @Override
            public int size() {
                return (int) values.getArraySize();
            }

            @Override
            public Object get(int index) {
                Value value = values.getArrayElement(index);
                if(value == null) {
                    return null;
                } else if (value.isHostObject()) {
                    return value.asHostObject();
                } else if (value.hasArrayElements()) {
                    return ValueListWrapper.fromValue(value);
                } else if (value.hasMembers()) {
                    return ValueMapWrapper.fromValue(value);
                } else if (value.isString()){
                    return value.asString();
                } else if (value.isNull() || "undefined".equals(value.toString())) { //add special case for undefined / null values otherwise callers can't do != null checks
                    return null;
                } else if (value.isNumber()){
                    if (value.fitsInInt()) {
                        return value.as(int.class);
                    } else if (value.fitsInByte()) {
                        return value.as(byte.class);
                    } else if (value.fitsInDouble()) {
                        return value.as(double.class);
                    } else if (value.fitsInFloat()) {
                        return value.as(float.class);
                    } else if (value.fitsInLong()) {
                        return value.as(long.class);
                    } else if (value.fitsInShort()) {
                        return value.as(short.class);
                    } else {
                        return value.as(double.class);
                    }
                } else if (value.isBoolean()){
                    return value.as(boolean.class);
                } else {
                    return value;
                }
            }
        };
    }
}
