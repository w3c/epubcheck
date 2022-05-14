package com.adobe.epubcheck.xml.handlers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableList;

public final class DelegateDefaultHandler extends DefaultHandler
{

  private final EntityResolver entityResolver;
  private final List<DTDHandler> dtdHandlers;
  private final List<ContentHandler> contentHandlers;
  private final List<ErrorHandler> errorHandlers;

  public static final class Builder
  {
    private EntityResolver entityResolver = null;
    private final List<DTDHandler> dtdHandlers = new LinkedList<>();
    private final List<ContentHandler> contentHandlers = new LinkedList<>();
    private final List<ErrorHandler> errorHandlers = new LinkedList<>();

    public void setEntityResolver(EntityResolver entityResolver)
    {
      this.entityResolver = entityResolver;
    }

    public void addDTDHandler(DTDHandler handler)
    {
      if (handler != null) this.dtdHandlers.add(handler);
    }

    public void addContentHandler(ContentHandler handler)
    {
      if (handler != null) this.contentHandlers.add(handler);
    }

    public void addErrorHandler(ErrorHandler handler)
    {
      if (handler != null) this.errorHandlers.add(handler);
    }

    public DelegateDefaultHandler build()
    {
      return new DelegateDefaultHandler(this);
    }
  }

  private DelegateDefaultHandler(Builder builder)
  {
    this.entityResolver = builder.entityResolver;
    this.dtdHandlers = ImmutableList.copyOf(builder.dtdHandlers);
    this.contentHandlers = ImmutableList.copyOf(builder.contentHandlers);
    this.errorHandlers = ImmutableList.copyOf(builder.errorHandlers);
  }

  /*------------------------------------
   *  EntityResolver implementation
   *------------------------------------*/
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException,
    IOException
  {
    if (entityResolver != null)
    {
      return entityResolver.resolveEntity(publicId, systemId);
    }
    return null;
  }
  /*------------------------------------
   *  DTDHandler implementation
   *------------------------------------*/

  @Override
  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    for (DTDHandler delegate : dtdHandlers)
    {
      delegate.notationDecl(name, publicId, systemId);
    }

  }

  @Override
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    for (DTDHandler delegate : dtdHandlers)
    {
      delegate.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  /*------------------------------------
   *  ContentHandler implementation
   *------------------------------------*/

  @Override
  public void setDocumentLocator(Locator locator)
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.setDocumentLocator(locator);
    }
  }

  @Override
  public void startDocument()
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.startDocument();
    }
  }

  @Override
  public void endDocument()
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.endDocument();
    }

  }

  @Override
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.startPrefixMapping(prefix, uri);
    }

  }

  @Override
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.endPrefixMapping(prefix);
    }

  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.startElement(uri, localName, qName, atts);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.endElement(uri, localName, qName);
    }

  }

  @Override
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.characters(ch, start, length);
    }
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.ignorableWhitespace(ch, start, length);
    }
  }

  @Override
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.processingInstruction(target, data);
    }
  }

  @Override
  public void skippedEntity(String name)
    throws SAXException
  {
    for (ContentHandler delegate : contentHandlers)
    {
      delegate.skippedEntity(name);
    }
  }

  /*------------------------------------
   *  ErrorHandler implementation
   *------------------------------------*/

  @Override
  public void warning(SAXParseException ex)
    throws SAXException
  {
    for (ErrorHandler delegate : errorHandlers)
    {
      delegate.warning(ex);
    }
  }

  @Override
  public void error(SAXParseException ex)
    throws SAXException
  {
    for (ErrorHandler delegate : errorHandlers)
    {
      delegate.error(ex);
    }
  }

  @Override
  public void fatalError(SAXParseException ex)
    throws SAXException
  {
    for (ErrorHandler delegate : errorHandlers)
    {
      delegate.fatalError(ex);
    }
  }

}
