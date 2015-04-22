package com.sandbox.runtime.utils;

import com.sandbox.runtime.models.XPathNode;
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

    static Map<String, Method> cachedMethodMap = new HashMap<String, Method>();

    Object target = null;
    XPathNode xPathNode;

    public XMLNodeInvocationHandler(Node target) {
        this.target = target;
        this.xPathNode = new XPathNode(target);
    }

    public Object invoke(Object proxy, Method proxyMethod, Object[] args)
            throws Throwable {
        String methodName = proxyMethod.getName();

        //if someone calls the text() method, reroute it to getTextContent() for backwards compat.
        if("text".equals(methodName)){
            methodName = "getTextContent";

        }else if("get".equals(methodName)){
            //if .get() is call on node, route to xpathnode obj
            return handleReturnValue(xPathNode.get((String) args[0]));

        }else if("find".equals(methodName)){
            //if .find() is call on node, route to xpathnode obj
            return handleReturnValue(xPathNode.find((String) args[0]));

        }else if("toString".equals(methodName)){
            //if .find() is call on node, route to xpathnode obj
            return handleReturnValue(xPathNode.toString());

        }

        //if we have no target, return null, can't throw an exp as JS will flip out.
        if(target == null) return null;

        Method targetMethod = null;
        String cacheKey = target.getClass().getCanonicalName()+proxyMethod;

        if (!cachedMethodMap.containsKey(cacheKey)) {
            targetMethod = target.getClass().getMethod(methodName, proxyMethod.getParameterTypes());
            cachedMethodMap.put(cacheKey, targetMethod);
        } else {
            targetMethod = cachedMethodMap.get(cacheKey);
        }
        Object retVal = targetMethod.invoke(target, args);

        return handleReturnValue(retVal);
    }

    private Object handleReturnValue(Object retVal){
        if(retVal instanceof Node) retVal = XMLNodeInvocationHandler.wrap((Node) retVal);
        return retVal;
    }

    public static EnhancedXMLNode wrap(Node node){
        return (EnhancedXMLNode) Proxy.newProxyInstance(EnhancedXMLNode.class.getClassLoader(), new Class[]{ EnhancedXMLNode.class }, new XMLNodeInvocationHandler(node));
    }


}
