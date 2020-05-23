package com.sandbox.worker.core.graal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.graalvm.polyglot.Value;

public interface ValueMapWrapper extends Map<String, Object> {

    static ValueMapWrapper fromValue(Value values) {
        return new ValueMapWrapper() {

            @Override
            public int size() {
                return values.getMemberKeys().size();
            }

            @Override
            public boolean isEmpty() {
                return !values.hasMembers();
            }

            @Override
            public boolean containsKey(Object key) {
                return values.hasMember((String) key);
            }

            @Override
            public boolean containsValue(Object value) {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public Object get(Object key) {
                Value value = values.getMember((String) key);
                if(value == null) {
                    return null;
                } else if (value.hasArrayElements()) {
                    return ValueListWrapper.fromValue(value);
                } else if (value.hasMembers()) {
                    return ValueMapWrapper.fromValue(value);
                } else if ("undefined".equals(value.toString())) { //add special case for undefined / null values otherwise callers can't do != null checks
                    return null;
                } else {
                    return value;
                }
            }

            @Override
            public Object put(String key, Object value) {
                values.putMember(key, value);
                return value;
            }

            @Override
            public Object remove(Object key) {
                Object value = get(key);
                values.removeMember((String) key);
                return value;
            }

            @Override
            public void putAll(Map m) {
                m.forEach((k, v) -> values.putMember(String.valueOf(k), v));
            }

            @Override
            public void clear() {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public Set keySet() {
                return values.getMemberKeys();
            }

            @Override
            public Collection values() {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                throw new RuntimeException("Not implemented");
            }

        };
    }
}
