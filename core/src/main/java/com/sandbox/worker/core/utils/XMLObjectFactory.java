package com.sandbox.worker.core.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;

public class XMLObjectFactory {

    private static DocumentBuilderFactory documentBuilderFactory;

    static {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
    }

    public static DocumentBuilder xmlDocumentBuilder() {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static XPathFactory xPathFactory() {
        return XPathFactory.newInstance();
    }

}
