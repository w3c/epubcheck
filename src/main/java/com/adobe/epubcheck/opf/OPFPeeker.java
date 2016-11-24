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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFData.OPFDataBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.google.common.io.Closer;

public final class OPFPeeker
{
  private final static String FINISHED_PARSING = "FINISHED_PARSING";

  private final Report report;
  private final String path;
  private final GenericResourceProvider resourceProvider;

  public OPFPeeker(String path, Report report,
      GenericResourceProvider resourceProvider)
  {
    this.path = path;
    this.report = report;
    this.resourceProvider = resourceProvider;
  }

  public OPFData peek()
    throws InvalidVersionException,
    IOException
  {
    Closer closer = Closer.create();
    try
    {
      InputStream in = resourceProvider.getInputStream(path);
      if (in == null)
        throw new IOException("Couldn't find resource " + path);
      in = closer.register(resourceProvider.getInputStream(path));
      return peek(in);
    } catch (Throwable e)
    {
      throw closer.rethrow(e, InvalidVersionException.class);
    } finally
    {
      closer.close();
    }
  }

  private OPFData peek(InputStream inputStream)
    throws InvalidVersionException
  {
    OPFDataBuilder builder = new OPFDataBuilder();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    try
    {
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
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
      report.message(MessageId.RSC_005, EPUBLocation.create(path),
          e.getMessage());
    } catch (InvalidVersionException e)
    {
      throw e;
    } catch (SAXException e)
    {
      if (FINISHED_PARSING.equals(e.getMessage()))
      {
        OPFData data = builder.build();
        return data;
      } else
      {
        report.message(MessageId.RSC_005, EPUBLocation.create(path),
            e.getMessage());
      }
    } catch (IOException e)
    {
      report
          .message(MessageId.PKG_008, EPUBLocation.create(path), path);
    }
    throw new InvalidVersionException(InvalidVersionException.VERSION_NOT_FOUND);
  }

  private static class ParserHandler extends DefaultHandler
  {

    private static final String VERSION_3 = "3.0";
    private static final String VERSION_2 = "2.0";

    private final OPFDataBuilder builder;
    private boolean isPackageRoot = false;
    private String currentText = null;
    private String uniqueId = null;
    private boolean isUniqueId = false;

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
        isPackageRoot = true;
      } else if (!isPackageRoot)
      {
        throw new InvalidVersionException(
            InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      } else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = "";
      } else if ("identifier".equals(localName) && EpubConstants.DCElements.equals(uri)) {
        String id  = attributes.getValue("id");
        isUniqueId = id!=null && id.trim().equals(uniqueId); 
        if (isUniqueId) {
          currentText = "";
        }
      }
    }

    @Override
    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
      if (currentText != null)
      {
        currentText += String.valueOf(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      if ("metadata".equals(localName) || "package".equals(localName))
      {
        throw new SAXException(OPFPeeker.FINISHED_PARSING);
      } else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = currentText.trim();
        if (currentText.length() > 0)
          builder.withType(currentText);
        currentText = null;
      } else if (isUniqueId && "identifier".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = currentText.trim();
        if (currentText.length() > 0)
          builder.withUniqueId(currentText);
        isUniqueId = false;
        currentText = null;
      }
    }

    private void processPackage(Attributes attributes)
      throws SAXException
    {
      String version = attributes.getValue("version");
      if (version == null)
      {
        throw new InvalidVersionException(
            InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      } else if (VERSION_3.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_3);
      } else if (VERSION_2.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_2);
      } else
      {
        throw new InvalidVersionException(
            InvalidVersionException.UNSUPPORTED_VERSION);
      }
      String uniqueId = attributes.getValue("unique-identifier");
      if (uniqueId != null)
        this.uniqueId  = uniqueId;
    }
  }

}
