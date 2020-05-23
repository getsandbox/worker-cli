package com.sandbox.worker.core.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class FormatUtils {

    public boolean isXml(Map<String, String> headers){
        if(headers != null && headers.get("Content-Type") != null && (headers.get("Content-Type").contains("application/soap") || headers.get("Content-Type").contains("application/xml") || headers.get("Content-Type").contains("text/xml"))){
            return true;
        }else{
            return false;
        }
    }

    public String formatXml(String xml) {

        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
            final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

            //May need this: System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");


            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.

            return writer.writeToString(document);
        } catch (Exception e) {
            //if formatting fails just return the unformatted version
            return xml;
        }
    }
}
