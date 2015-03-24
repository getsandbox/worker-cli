package com.sandbox.runtime.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickhoughton on 10/08/2014.
 */
public class XMLNodeListInvocationHandler implements InvocationHandler {

    static Map<Method, Method> cachedMethodMap = new HashMap<Method, Method>();

    Object target = null;

    public XMLNodeListInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method proxyMethod, Object[] args)
            throws Throwable {
        String methodName = proxyMethod.getName();

        //if we have no target, return null, can't throw an exp as JS will flip out.
        if(target == null) return null;

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

    public static NodeList wrap(NodeList node){
        return (NodeList) Proxy.newProxyInstance(NodeList.class.getClassLoader(), new Class[]{ NodeList.class }, new XMLNodeListInvocationHandler(node));
    }


}
