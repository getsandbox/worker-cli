package com.sandbox.runtime.models;

import com.sandbox.runtime.config.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;

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
public class XMLDoc extends XPathNode{

    //this is crap, need to fix
    DocumentBuilder db = Context.xmlDocumentBuilder();

    Document doc;
    NodeList nodes;
    String docString;

    public XMLDoc(Object body) throws Exception{
        super();
        try {
            doc = db.parse(new InputSource(new StringReader((String) body)));
            nodes = doc.getChildNodes();
            super.setNode(doc);
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

    public Element root() {
        return doc.getDocumentElement();
    }

}