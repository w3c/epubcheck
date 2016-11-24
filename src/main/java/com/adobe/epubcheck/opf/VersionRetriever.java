/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.opf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.InvalidVersionException;

public class VersionRetriever implements EntityResolver, ErrorHandler
{
  private static final String VERSION_3 = "3.0";
  private static final String VERSION_2 = "2.0";
  private final Report report;
  private final String path;

  public VersionRetriever(String path, Report report)
  {
    this.path = path;
    this.report = report;
  }

  public EPUBVersion retrieveOpfVersion(InputStream inputStream)
      throws
      InvalidVersionException
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    try
    {
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    }
    catch (Exception ignored)
    {
    }

    SAXParser parser;
    try
    {
      parser = factory.newSAXParser();
      parser.getXMLReader().setEntityResolver(this);
      parser.getXMLReader().setErrorHandler(this);
      parser.getXMLReader().setContentHandler(new OPFhandler());
      parser.getXMLReader().parse(new InputSource(inputStream));
    }
    catch (ParserConfigurationException e)
    {
      report.message(MessageId.RSC_005, EPUBLocation.create(path), e.getMessage());
    }
    catch (SAXException e)
    {
      if (VERSION_3.equals(e.getMessage()))
      {
        report.info(null, FeatureEnum.FORMAT_VERSION, EPUBVersion.VERSION_3.toString());
        return EPUBVersion.VERSION_3;
      }
      else if (VERSION_2.equals(e.getMessage()))
      {
        report.info(null, FeatureEnum.FORMAT_VERSION, EPUBVersion.VERSION_2.toString());
        return EPUBVersion.VERSION_2;
      }
      else if (InvalidVersionException.UNSUPPORTED_VERSION.equals(e.getMessage()))
      {
        throw new InvalidVersionException(InvalidVersionException.UNSUPPORTED_VERSION);
      }
      else if (InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND.equals(e.getMessage()))
      {
        throw new InvalidVersionException(InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      }
      else if (InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND.equals(e.getMessage()))
      {
        throw new InvalidVersionException(InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      }
      else
      {
        report.message(MessageId.RSC_005, EPUBLocation.create(path), e.getMessage());
      }
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(path), path);
    }
    throw new InvalidVersionException(InvalidVersionException.VERSION_NOT_FOUND);
  }

  @Override
  public InputSource resolveEntity(String arg0, String arg1) throws
      SAXException,
      IOException
  {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void error(SAXParseException arg0) throws
      SAXException
  {
  }

  @Override
  public void fatalError(SAXParseException arg0) throws
      SAXException
  {
  }

  @Override
  public void warning(SAXParseException arg0) throws
      SAXException
  {
  }

  private class OPFhandler extends DefaultHandler
  {
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws
        SAXException
    {
      if ("package".equals(localName))
      {
        processPackage(attributes);
      }
      else
      {
        throw new SAXException(
            InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      }
    }

    private void processPackage(Attributes attributes) throws
        SAXException
    {
      String version = attributes.getValue("version");
      if (version == null)
      {
        throw new SAXException(
            InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      }
      else if (VERSION_3.equals(version))
      {
        throw new SAXException(VERSION_3);
      }
      else if (VERSION_2.equals(version))
      {
        throw new SAXException(VERSION_2);
      }
      throw new SAXException(InvalidVersionException.UNSUPPORTED_VERSION);
    }
  }
}
