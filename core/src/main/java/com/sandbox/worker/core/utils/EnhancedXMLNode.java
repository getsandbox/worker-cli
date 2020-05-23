package com.sandbox.worker.core.utils;

import org.graalvm.polyglot.HostAccess;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

@HostAccess.Implementable
public class EnhancedXMLNode implements Node {

    private final Node delegate;
    private final XPathNode xPathNode;

    public EnhancedXMLNode(Node delegate) {
        if(delegate == null) throw new IllegalArgumentException("delegate cant be null");
        this.delegate = delegate;
        this.xPathNode = new XPathNode(delegate);
    }

    private Node handleReturnValue(Node retVal){
        if(!(retVal instanceof EnhancedXMLNode) && retVal != null) retVal = new EnhancedXMLNode(retVal);
        return retVal;
    }

    private NodeList handleReturnValue(NodeList retVal){
        if(!(retVal instanceof EnhancedXMLNodeList) && retVal != null) retVal = new EnhancedXMLNodeList(retVal);
        return retVal;
    }

    @HostAccess.Export
    public String text() {
        return delegate.getTextContent();
    }

    @HostAccess.Export
    public Node get(String searchString) {
        return handleReturnValue(xPathNode.get(searchString));
    }

    @HostAccess.Export
    public NodeList find(String searchString) {
        return handleReturnValue(xPathNode.find(searchString));
    }

    @HostAccess.Export
    @Override
    public String toString() {
        return xPathNode.toString();
    }

    @HostAccess.Export
    @Override
    public String getNodeName() {
        return delegate.getNodeName();
    }

    @HostAccess.Export
    @Override
    public String getNodeValue() throws DOMException {
        return delegate.getNodeValue();
    }

    @HostAccess.Export
    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        delegate.setNodeValue(nodeValue);
    }

    @HostAccess.Export
    @Override
    public short getNodeType() {
        return delegate.getNodeType();
    }

    @HostAccess.Export
    @Override
    public Node getParentNode() {
        return delegate.getParentNode();
    }

    @HostAccess.Export
    @Override
    public NodeList getChildNodes() {
        return delegate.getChildNodes();
    }

    @HostAccess.Export
    @Override
    public Node getFirstChild() {
        return delegate.getFirstChild();
    }

    @HostAccess.Export
    @Override
    public Node getLastChild() {
        return delegate.getLastChild();
    }

    @HostAccess.Export
    @Override
    public Node getPreviousSibling() {
        return delegate.getPreviousSibling();
    }

    @HostAccess.Export
    @Override
    public Node getNextSibling() {
        return delegate.getNextSibling();
    }

    @HostAccess.Export
    @Override
    public NamedNodeMap getAttributes() {
        return delegate.getAttributes();
    }

    @HostAccess.Export
    @Override
    public Document getOwnerDocument() {
        return delegate.getOwnerDocument();
    }

    @HostAccess.Export
    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return delegate.insertBefore(newChild, refChild);
    }

    @HostAccess.Export
    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return delegate.replaceChild(newChild, oldChild);
    }

    @HostAccess.Export
    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return delegate.removeChild(oldChild);
    }

    @HostAccess.Export
    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return delegate.appendChild(newChild);
    }

    @HostAccess.Export
    @Override
    public boolean hasChildNodes() {
        return delegate.hasChildNodes();
    }

    @HostAccess.Export
    @Override
    public Node cloneNode(boolean deep) {
        return delegate.cloneNode(deep);
    }

    @HostAccess.Export
    @Override
    public void normalize() {
        delegate.normalize();
    }

    @HostAccess.Export
    @Override
    public boolean isSupported(String feature, String version) {
        return delegate.isSupported(feature, version);
    }

    @HostAccess.Export
    @Override
    public String getNamespaceURI() {
        return delegate.getNamespaceURI();
    }

    @HostAccess.Export
    @Override
    public String getPrefix() {
        return delegate.getPrefix();
    }

    @HostAccess.Export
    @Override
    public void setPrefix(String prefix) throws DOMException {
        delegate.setPrefix(prefix);
    }

    @HostAccess.Export
    @Override
    public String getLocalName() {
        return delegate.getLocalName();
    }

    @HostAccess.Export
    @Override
    public boolean hasAttributes() {
        return delegate.hasAttributes();
    }

    @HostAccess.Export
    @Override
    public String getBaseURI() {
        return delegate.getBaseURI();
    }

    @HostAccess.Export
    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return delegate.compareDocumentPosition(other);
    }

    @HostAccess.Export
    @Override
    public String getTextContent() throws DOMException {
        return delegate.getTextContent();
    }

    @HostAccess.Export
    @Override
    public void setTextContent(String textContent) throws DOMException {
        delegate.setTextContent(textContent);
    }

    @HostAccess.Export
    @Override
    public boolean isSameNode(Node other) {
        return delegate.isSameNode(other);
    }

    @HostAccess.Export
    @Override
    public String lookupPrefix(String namespaceURI) {
        return delegate.lookupPrefix(namespaceURI);
    }

    @HostAccess.Export
    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return delegate.isDefaultNamespace(namespaceURI);
    }

    @HostAccess.Export
    @Override
    public String lookupNamespaceURI(String prefix) {
        return delegate.lookupNamespaceURI(prefix);
    }

    @HostAccess.Export
    @Override
    public boolean isEqualNode(Node arg) {
        return delegate.isEqualNode(arg);
    }

    @HostAccess.Export
    @Override
    public Object getFeature(String feature, String version) {
        return delegate.getFeature(feature, version);
    }

    @HostAccess.Export
    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return delegate.setUserData(key, data, handler);
    }

    @HostAccess.Export
    @Override
    public Object getUserData(String key) {
        return delegate.getUserData(key);
    }
}
