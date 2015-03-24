package com.sandbox.runtime.models;

import com.sandbox.runtime.config.Context;
import com.sandbox.runtime.utils.XMLNodeInvocationHandler;
import com.sandbox.runtime.utils.XMLNodeListInvocationHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringWriter;

/**
 * Created by nickhoughton on 22/04/2015.
 */
public class XPathNode {

    XPathFactory xPathFactory = Context.xPathFactory();
    XPath xPath;
    Node node;
    boolean cached = false;
    String docString = null;

    public XPathNode(Node node) {
        this.node = node;
        xPath = xPathFactory.newXPath();
    }

    public XPathNode() {
        xPath = xPathFactory.newXPath();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Returns the first node matching the xPath search string
     * @param searchString
     * @return Node
     */
    public <T> T get(String searchString, QName type, Class<T> returnType) {
        Object obj = null;
        try {
            obj = xPath.evaluate(searchString, node, type);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if(obj == null) return null;

        if(returnType == Node.class){
            return returnType.cast(XMLNodeInvocationHandler.wrap((Node) (obj)));
        }else{
            return returnType.cast(obj);
        }
    }

    public Node get(String searchString) {
        return get(searchString, XPathConstants.NODE, Node.class);
    }

    /**
     * Returns all nodes matching the xpath search string
     * @param searchString
     * @return NodeList
     */
    public NodeList find(String searchString) {
        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.evaluate(searchString, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return XMLNodeListInvocationHandler.wrap(nodeList);
    }

    @Override
    public String toString() {
        if (!cached) {
            try {
                DOMSource domSource = new DOMSource(node);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);
                docString = writer.toString();
            } catch (TransformerException ex) {
                ex.printStackTrace();
                docString = null;
            }
            cached = true;
        }
        return docString;
    }
}
