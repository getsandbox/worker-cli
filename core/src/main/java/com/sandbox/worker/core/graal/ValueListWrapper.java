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
                if (value.hasArrayElements()) {
                    return ValueListWrapper.fromValue(value);
                } else if (value.hasMembers()) {
                    return ValueMapWrapper.fromValue(value);
                } else if ("undefined".equals(value.toString())) { //add special case for undefined / null values otherwise callers can't do != null checks
                    return null;
                } else {
                    return value;
                }
            }
        };
    }
}
