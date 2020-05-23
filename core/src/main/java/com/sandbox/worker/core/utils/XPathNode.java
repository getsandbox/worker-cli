package com.sandbox.worker.core.utils;

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
import org.graalvm.polyglot.HostAccess;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@HostAccess.Implementable
public class XPathNode {

    private XPathFactory xPathFactory = XMLObjectFactory.xPathFactory();
    private XPath xPath;
    private Node node;

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
     *
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
        if (obj == null) return null;

        if (returnType == Node.class && obj != null) {
            return returnType.cast(new EnhancedXMLNode((Node) (obj)));
        } else if (returnType == NodeList.class && obj != null) {
            return returnType.cast(new EnhancedXMLNodeList((NodeList) (obj)));
        } else {
            return returnType.cast(obj);
        }
    }

    @HostAccess.Export
    public Node get(String searchString) {
        return get(searchString, XPathConstants.NODE, Node.class);
    }

    /**
     * Returns all nodes matching the xpath search string
     *
     * @param searchString
     * @return NodeList
     */
    @HostAccess.Export
    public NodeList find(String searchString) {
        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.evaluate(searchString, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList != null ? new EnhancedXMLNodeList(nodeList) : nodeList;
    }

    @HostAccess.Export
    @Override
    public String toString() {
        try {
            DOMSource domSource = new DOMSource(node);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
