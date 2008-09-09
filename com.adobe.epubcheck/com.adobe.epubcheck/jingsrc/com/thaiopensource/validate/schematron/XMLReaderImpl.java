package com.thaiopensource.validate.schematron;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;

abstract class XMLReaderImpl implements XMLReader {
  private ErrorHandler errorHandler;
  private DTDHandler dtdHandler;
  private EntityResolver entityResolver;

  public void parse(String systemId)
          throws SAXException, IOException {
    parse(new InputSource(systemId));
  }


  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public void setDTDHandler(DTDHandler handler) {
    this.dtdHandler = handler;
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  public Object getProperty(String name)
          throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException(name);
  }

  public void setProperty(String name, Object value)
          throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException(name);
  }

  public boolean getFeature(String name)
          throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name.equals("http://xml.org/sax/features/namespaces"))
      return true;
    if (name.equals("http://xml.org/sax/features/namespace-prefixes"))
      return false;
    throw new SAXNotRecognizedException(name);
  }

  public void setFeature(String name, boolean value)
          throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name.equals("http://xml.org/sax/features/namespaces")) {
      if (value == true)
        return;
      throw new SAXNotSupportedException(name);
    }
    if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
      if (value == false)
        return;
      throw new SAXNotSupportedException(name);
    }
    throw new SAXNotRecognizedException(name);
  }
}
