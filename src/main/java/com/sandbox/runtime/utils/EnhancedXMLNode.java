package com.sandbox.runtime.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by nickhoughton on 10/08/2014.
 */
public interface EnhancedXMLNode extends Node {

    public String text();
    public Node get(String searchString);
    public NodeList find(String searchString);
}
