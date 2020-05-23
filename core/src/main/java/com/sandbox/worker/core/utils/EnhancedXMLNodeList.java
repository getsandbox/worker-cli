package com.sandbox.worker.core.utils;

import com.sandbox.worker.core.graal.HostAccessDrivenProxyObject;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@HostAccess.Implementable
public class EnhancedXMLNodeList implements NodeList, ProxyArray, HostAccessDrivenProxyObject {
    private final NodeList delegate;

    public EnhancedXMLNodeList(NodeList delegate) {
        this.delegate = delegate;
    }

    @HostAccess.Export
    @Override
    public Node item(int index) {
        Node result = delegate.item(index);
        if(!(result instanceof EnhancedXMLNode)) result = new EnhancedXMLNode(delegate.item(index));
        return result;
    }

    @HostAccess.Export
    @Override
    public int getLength() {
        return delegate.getLength();
    }

    @Override
    public Object get(long index) {
        return item((int) index);
    }

    @Override
    public void set(long index, Value value) {
        //noop
    }

    @Override
    public long getSize() {
        return getLength();
    }
}
