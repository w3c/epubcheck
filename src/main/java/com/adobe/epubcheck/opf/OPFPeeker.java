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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.xml.SAXAbortException;
import com.adobe.epubcheck.xml.XMLParser;

public final class OPFPeeker
{
  private final Report report;
  private final String path;
  private final GenericResourceProvider resourceProvider;

  public OPFPeeker(String path, Report report, GenericResourceProvider resourceProvider)
  {
    this.path = path;
    this.report = report;
    this.resourceProvider = resourceProvider;
  }
  
  public OPFData peek()
    throws InvalidVersionException
  {
    OPFData.Builder builder = new OPFData.Builder();
    XMLParser parser = new XMLParser(new ValidationContextBuilder().path(path).report(report)
        .resourceProvider(resourceProvider).build());
    ParserHandler handler = new ParserHandler(builder);
    parser.setReporting(false);
    parser.addContentHandler(handler);
    parser.process();
    return builder.build();
  }

  private static class ParserHandler extends DefaultHandler
  {

    private static final String VERSION_3 = "3.0";
    private static final String VERSION_2 = "2.0";

    private final OPFData.Builder builder;
    private boolean isPackageRoot = false;
    private String currentText = null;
    private String uniqueId = null;
    private boolean isUniqueId = false;

    public ParserHandler(OPFData.Builder builder)
    {
      this.builder = builder;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      if ("package".equals(localName))
      {
        processPackage(attributes);
        isPackageRoot = true;
      }
      else if (!isPackageRoot)
      {
        builder.withError(InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
        throw new SAXAbortException();
      }
      else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = "";
      }
      else if ("identifier".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        String id = attributes.getValue("id");
        isUniqueId = id != null && id.trim().equals(uniqueId);
        if (isUniqueId)
        {
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
        throw new SAXAbortException();
      }
      else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = currentText.trim();
        if (currentText.length() > 0) builder.withType(currentText);
        currentText = null;
      }
      else if (isUniqueId && "identifier".equals(localName) && EpubConstants.DCElements.equals(uri))
      {
        currentText = currentText.trim();
        if (currentText.length() > 0) builder.withUniqueId(currentText);
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

        builder.withError(InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
        throw new SAXAbortException();
      }
      else if (VERSION_3.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_3);
      }
      else if (VERSION_2.equals(version))
      {
        builder.withVersion(EPUBVersion.VERSION_2);
      }
      else
      {

        builder.withError(InvalidVersionException.UNSUPPORTED_VERSION);
        throw new SAXAbortException();
      }
      String uniqueId = attributes.getValue("unique-identifier");
      if (uniqueId != null) this.uniqueId = uniqueId;
    }
  }

}
