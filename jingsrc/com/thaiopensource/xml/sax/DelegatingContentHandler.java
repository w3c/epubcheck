package com.thaiopensource.xml.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class DelegatingContentHandler implements ContentHandler {
  private ContentHandler delegate;

  public DelegatingContentHandler() {
  }

  public DelegatingContentHandler(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public ContentHandler getDelegate() {
    return delegate;
  }

  public void setDelegate(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public void setDocumentLocator (Locator locator) {
    if (delegate != null)
      delegate.setDocumentLocator(locator);
  }

  public void startDocument ()
      throws SAXException {
    if (delegate != null)
      delegate.startDocument();
  }

  public void endDocument()
      throws SAXException {
    if (delegate != null)
      delegate.endDocument();
  }

  public void startPrefixMapping (String prefix, String uri)
      throws SAXException {
    if (delegate != null)
      delegate.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping (String prefix)
      throws SAXException {
    if (delegate != null)
      delegate.endPrefixMapping(prefix);
  }

  public void startElement (String namespaceURI, String localName,
                            String qName, Attributes atts)
      throws SAXException {
    if (delegate != null)
      delegate.startElement(namespaceURI, localName, qName, atts);
  }

  public void endElement (String namespaceURI, String localName,
                          String qName)
      throws SAXException {
    if (delegate != null)
      delegate.endElement(namespaceURI, localName, qName);
  }

  public void characters (char ch[], int start, int length)
      throws SAXException {
    if (delegate != null)
      delegate.characters(ch, start, length);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
      throws SAXException {
    if (delegate != null)
      delegate.ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction (String target, String data)
      throws SAXException {
    if (delegate != null)
      delegate.processingInstruction(target, data);
  }

  public void skippedEntity (String name)
      throws SAXException {
    if (delegate != null)
      delegate.skippedEntity(name);
  }
}
