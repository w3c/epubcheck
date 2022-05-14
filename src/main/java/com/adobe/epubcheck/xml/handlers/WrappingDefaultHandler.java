package com.adobe.epubcheck.xml.handlers;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;

public class WrappingDefaultHandler extends DefaultHandler
{

  private final DefaultHandler wrapped;

  public WrappingDefaultHandler(DefaultHandler handler)
  {
    Preconditions.checkNotNull(handler);
    this.wrapped = handler;
  }

  /*------------------------------------
   *  EntityResolver implementation
   *------------------------------------*/
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException,
    IOException
  {
    return wrapped.resolveEntity(publicId, systemId);
  }
  /*------------------------------------
   *  DTDHandler implementation
   *------------------------------------*/

  @Override
  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    wrapped.notationDecl(name, publicId, systemId);
  }

  @Override
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    wrapped.unparsedEntityDecl(name, publicId, systemId, notationName);
  }

  /*------------------------------------
   *  ContentHandler implementation
   *------------------------------------*/

  @Override
  public void setDocumentLocator(Locator locator)
  {
    wrapped.setDocumentLocator(locator);
  }

  @Override
  public void startDocument()
    throws SAXException
  {
    wrapped.startDocument();
  }

  @Override
  public void endDocument()
    throws SAXException
  {
    wrapped.endDocument();
  }

  @Override
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    wrapped.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    wrapped.endPrefixMapping(prefix);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    wrapped.startElement(uri, localName, qName, atts);
  }

  @Override
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    wrapped.endElement(uri, localName, qName);
  }

  @Override
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    wrapped.characters(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    wrapped.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    wrapped.processingInstruction(target, data);
  }

  @Override
  public void skippedEntity(String name)
    throws SAXException
  {
    wrapped.skippedEntity(name);
  }

  /*------------------------------------
   *  ErrorHandler implementation
   *------------------------------------*/

  @Override
  public void warning(SAXParseException ex)
    throws SAXException
  {
    wrapped.warning(ex);
  }

  @Override
  public void error(SAXParseException ex)
    throws SAXException
  {
    wrapped.error(ex);
  }

  @Override
  public void fatalError(SAXParseException ex)
    throws SAXException
  {
    wrapped.fatalError(ex);
  }

}
