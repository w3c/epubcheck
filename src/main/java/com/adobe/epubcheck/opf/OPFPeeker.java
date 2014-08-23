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
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.OPFData.OPFDataBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.InvalidVersionException;

public final class OPFPeeker
{
  private final static String FINISHED_PARSING = "FINISHED_PARSING";

  private final Report report;
  private final String path;

  public OPFPeeker(String path, Report report)
  {
    this.path = path;
    this.report = report;
  }

  public OPFData peek(InputStream inputStream)
    throws InvalidVersionException
  {
    OPFDataBuilder builder = new OPFDataBuilder();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    try
    {
      factory.setFeature("http://xml.org/sax/features/validation", false);
    } catch (Exception ignored)
    {
    }

    SAXParser parser;
    try
    {
      ParserHandler handler = new ParserHandler(builder);
      parser = factory.newSAXParser();
      parser.getXMLReader().setEntityResolver(handler);
      parser.getXMLReader().setErrorHandler(handler);
      parser.getXMLReader().setContentHandler(handler);
      parser.getXMLReader().parse(new InputSource(inputStream));
    } catch (ParserConfigurationException e)
    {
      report.message(MessageId.RSC_005, new MessageLocation(path, -1, -1),
          e.getMessage());
    } catch (SAXException e)
    {
      if (FINISHED_PARSING.equals(e.getMessage()))
      {
        OPFData data = builder.build();
        return data;
      } else if (InvalidVersionException.UNSUPPORTED_VERSION.equals(e
          .getMessage()))
      {
        throw new InvalidVersionException(
            InvalidVersionException.UNSUPPORTED_VERSION);
      } else if (InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND.equals(e
          .getMessage()))
      {
        throw new InvalidVersionException(
            InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      } else if (InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND.equals(e
          .getMessage()))
      {
        throw new InvalidVersionException(
            InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      } else
      {
        report.message(MessageId.RSC_005, new MessageLocation(path, -1, -1),
            e.getMessage());
      }
    } catch (IOException e)
    {
      report
          .message(MessageId.PKG_008, new MessageLocation(path, -1, -1), path);
    }
    throw new InvalidVersionException(InvalidVersionException.VERSION_NOT_FOUND);
  }

  private static class ParserHandler extends DefaultHandler implements
      EntityResolver, ErrorHandler
  {

    private static final String VERSION_3 = "3.0";
    private static final String VERSION_2 = "2.0";

    private final OPFDataBuilder builder;
    private boolean packageRoot = false;

    public ParserHandler(OPFDataBuilder builder)
    {
      this.builder = builder;
    }
    
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
      throws IOException,
      SAXException
    {
      return new InputSource(new StringReader(""));
    }

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes attributes)
      throws SAXException
    {
      if ("package".equals(localName))
      {
        processPackage(attributes);
        packageRoot = true;
      } else if (!packageRoot)
      {
        throw new SAXException(
            InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      }
      // TODO support peeking other stuff, like dc:type
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      if ("metadata".equals(localName) || "package".equals(localName))
      {
        throw new SAXException(OPFPeeker.FINISHED_PARSING);
      }
    }

    private void processPackage(Attributes attributes)
      throws SAXException
    {
      String version = attributes.getValue("version");
      if (version == null)
      {
        throw new SAXException(
            InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      } else if (VERSION_3.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_3);
      } else if (VERSION_2.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_2);
      } else
      {
        throw new SAXException(InvalidVersionException.UNSUPPORTED_VERSION);
      }
    }
  }

}
