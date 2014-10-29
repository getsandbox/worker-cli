package com.sandbox.runtime.utils;

import org.w3c.dom.Node;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickhoughton on 10/08/2014.
 */
public class XMLNodeInvocationHandler implements InvocationHandler {

    static Map<Method, Method> cachedMethodMap = new HashMap<Method, Method>();

    Object target = null;

    public XMLNodeInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method proxyMethod, Object[] args)
            throws Throwable {
        String methodName = proxyMethod.getName();

        //if someone calls the text() method, reroute it to getTextContent() for backwards compat.
        if("text".equals(methodName)){
            methodName = "getTextContent";
        }

        Method targetMethod = null;
        if (!cachedMethodMap.containsKey(proxyMethod)) {
            targetMethod = target.getClass().getMethod(methodName,
                    proxyMethod.getParameterTypes());
            cachedMethodMap.put(proxyMethod, targetMethod);
        } else {
            targetMethod = cachedMethodMap.get(proxyMethod);
        }
        Object retVal = targetMethod.invoke(target, args);

        if(retVal instanceof Node) retVal = XMLNodeInvocationHandler.wrap((Node) retVal);

        return retVal;
    }

    public static EnhancedXMLNode wrap(Node node){
        return (EnhancedXMLNode) Proxy.newProxyInstance(EnhancedXMLNode.class.getClassLoader(), new Class[]{ EnhancedXMLNode.class }, new XMLNodeInvocationHandler(node));
    }


}
