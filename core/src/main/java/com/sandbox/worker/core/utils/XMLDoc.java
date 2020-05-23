package com.sandbox.worker.core.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathConstants;
import java.io.StringReader;
import org.graalvm.polyglot.HostAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



/**
 * functions used by Auspost stubs:
 *
 * xmlDoc.get(..)
 * xmlDoc.toString(..)
 * xmlDoc.get(..).text()
 * test for xmlDoc.get(..) == null
 */
@HostAccess.Implementable
public class XMLDoc extends XPathNode {

    //this is crap, need to fix
    DocumentBuilder db = XMLObjectFactory.xmlDocumentBuilder();

    Document doc;
    NodeList nodes;

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
    @HostAccess.Export
    public Node child(int idx) {
        Node item = nodes.item(idx);
        return item != null && !(item instanceof EnhancedXMLNode) ? new EnhancedXMLNode(item) : item;
    }

    /**
     * get all childnodes
     * @return NodeList
     */
    @HostAccess.Export
    public NodeList childNodes() {
        return nodes != null && !(nodes instanceof EnhancedXMLNodeList) ? new EnhancedXMLNodeList(nodes) : nodes;
    }

    @HostAccess.Export
    public String encoding() {
        return doc.getXmlEncoding();
    }

    @HostAccess.Export
    public Element root() {
        return doc.getDocumentElement();
    }

    @HostAccess.Export
    public String getSOAPOperationName(){
        return get("local-name(//*[local-name()='Envelope']/*[local-name()='Body']/*[1])", XPathConstants.STRING, String.class);
    }

}