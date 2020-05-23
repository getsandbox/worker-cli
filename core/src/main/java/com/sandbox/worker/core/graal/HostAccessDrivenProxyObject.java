package com.sandbox.worker.core.graal;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

//shouldn't need this but works around the problem where you can't add custom methods to Proxy* objects
//as per https://github.com/graalvm/graaljs/issues/163
public interface HostAccessDrivenProxyObject extends ProxyObject {

    Map<Class, Map<String, Object>> accessibleObjectsForClass = new HashMap<>();

    default Map<String, Object> _getAccessibleObjects() {
        return accessibleObjectsForClass.computeIfAbsent(this.getClass(), (key) -> new HashMap<>());
    }

    default void _load() {
        Map<String, Object> accessibleObjects = _getAccessibleObjects();
        if (accessibleObjects.size() == 0) {
            Object self = this;
            Stream.of(this.getClass().getDeclaredMethods()).filter(m -> m.getAnnotation(HostAccess.Export.class) != null).forEach(m -> {
                ProxyExecutable proxyExecutable = arguments -> {
                    try {
                        if (arguments.length == 0) {
                            return m.invoke(self);
                        } else {
                            Object[] typedArguments = new Object[arguments.length];
                            for (int x = 0; x < arguments.length; x++) {
                                typedArguments[x] = arguments[x].as(m.getParameterTypes()[x]);
                            }
                            return m.invoke(self, typedArguments);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                };
                accessibleObjects.put(m.getName(), proxyExecutable);
            });
            Stream.of(this.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(HostAccess.Export.class) != null).forEach(f -> {
                try {
                    accessibleObjects.put(f.getName(), f.get(self));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    default Object getMember(String key) {
        _load();
        return _getAccessibleObjects().get(key);
    }

    @Override
    default Object getMemberKeys() {
        _load();
        return _getAccessibleObjects().keySet();
    }

    @Override
    default boolean hasMember(String key) {
        _load();
        return _getAccessibleObjects().containsKey(key);
    }

    @Override
    default void putMember(String key, Value value) {
        //noop
    }
}
