package com.sandbox.runtime.models;

import com.sandbox.runtime.config.Context;
import com.sandbox.runtime.utils.XMLNodeInvocationHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by drew on 3/08/2014.
 */

/**
 * functions used by Auspost stubs:
 *
 * xmlDoc.get(..)
 * xmlDoc.toString(..)
 * xmlDoc.get(..).text()
 * test for xmlDoc.get(..) == null
 */
public class XMLDoc {

    //this is crap, need to fix
    DocumentBuilder db = Context.xmlDocumentBuilder();

    XPathFactory xPathFactory = Context.xPathFactory();

    Document doc;
    XPath xPath;
    NodeList nodes;
    String docString;
    boolean cached = false;

    public XMLDoc(Object body) throws Exception{
        try {
            doc = db.parse(new InputSource(new StringReader((String) body)));
            xPath = xPathFactory.newXPath();
            nodes = doc.getChildNodes();
        } catch(Exception e) {
            System.out.println("Exception parsing XML body: " + e.getMessage());
            throw new Exception("Failed to parse xml body");
        }
    }

    /**
     * Convenience method to get the idxth child of the root element
     * @param idx
     * @return Node
     */
    public Node child(int idx) {
        return nodes.item(idx);
    }

    /**
     * get all childnodes
     * @return NodeList
     */
    public NodeList childNodes() {
        return nodes;
    }

    public String encoding() {
        return doc.getXmlEncoding();
    }

    /**
     * Returns the first node matching the xPath search string
     * @param searchString
     * @return Node
     */
    public Node get(String searchString) {
        Node node = null;
        try {
            node = (Node) xPath.evaluate(searchString, doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return XMLNodeInvocationHandler.wrap(node);
    }

    /**
     * Returns all nodes matching the xpath search string
     * @param searchString
     * @return NodeList
     */
    public NodeList find(String searchString) {
        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.evaluate(searchString, doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    public Element root() {
        return doc.getDocumentElement();
    }

    @Override
    public String toString() {
        if (!cached) {
            try {
                DOMSource domSource = new DOMSource(doc);
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