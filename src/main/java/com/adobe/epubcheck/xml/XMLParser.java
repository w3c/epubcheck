/*
 * Copyright (c) 2007 Adobe Systems Incorporated
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

package com.adobe.epubcheck.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.xml.handlers.DeclarationHandler;
import com.adobe.epubcheck.xml.handlers.DefaultResolver;
import com.adobe.epubcheck.xml.handlers.DelegateDefaultHandler;
import com.adobe.epubcheck.xml.handlers.PreprocessingDefaultHandler;
import com.adobe.epubcheck.xml.handlers.ReportingErrorHandler;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;

public class XMLParser
{

  private static final String SAXPROP_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
  private static final String SAXPROP_DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler";
  private static final String ZIP_ROOT = "file:///epub-root/";
  private final ValidationContext context;
  private final Report report;
  private final String path;
  private final SAXParser parser;
  private final DelegateDefaultHandler.Builder handler = new DelegateDefaultHandler.Builder();
  private boolean reporting = true;

  public XMLParser(ValidationContext context)
  {
    this.context = context;
    this.report = context.report;
    this.path = context.path;

    SAXParserFactory factory = SAXParserFactory.newInstance();

    try
    {
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      if (context.version == EPUBVersion.VERSION_3)
      {
        factory.setXIncludeAware(false);
      }
    } catch (Exception ignored)
    {
    }

    try
    {
      parser = factory.newSAXParser();
      handler.setEntityResolver(new DefaultResolver(context.version));

      XMLReader reader = parser.getXMLReader();
      DeclarationHandler docTypeHandler = new DeclarationHandler(context);
      reader.setProperty(SAXPROP_LEXICAL_HANDLER, docTypeHandler);
      reader.setProperty(SAXPROP_DECL_HANDLER, docTypeHandler);

    } catch (Exception e)
    {
      throw new AssertionError("Could not configure the XML parser", e);
    }
  }

  public void setReporting(boolean reporting)
  {
    this.reporting = reporting;
  }

  public void addContentHandler(ContentHandler contentHandler)
  {
    handler.addContentHandler(contentHandler);
  }

  public void addValidator(XMLValidator xv)
  {
    PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
    ErrorHandler eh = new ReportingErrorHandler(context, xv.isNormative());
    propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, eh);
    Validator validator = xv.getSchema().createValidator(propertyMapBuilder.toPropertyMap());
    handler.addContentHandler(validator.getContentHandler());
    handler.addDTDHandler(validator.getDTDHandler());
  }

  public void process()
  {
    try (InputStream in = context.resourceProvider.getInputStream(path);
        InputStream buffered = new BufferedInputStream(in))
    {
      if (in == null)
      {
        // Abort processing.
        // Missing required files are reported elsewhere.
        return;
      }

      // Check encoding
      String encoding = EncodingSniffer.sniffEncoding(buffered);
      if (encoding != null && !encoding.equals("UTF-8") && !encoding.equals("UTF-16"))
      {
        report.message(MessageId.CSS_003, EPUBLocation.create(path), encoding);
      }

      // Build the input source
      InputSource source = new InputSource(buffered);
      source.setSystemId(ZIP_ROOT + path);

      // Set the error handler
      if (reporting)
      {
        handler.addErrorHandler(new ReportingErrorHandler(context));
      }

      // Parse
      parser.parse(source, new PreprocessingDefaultHandler(handler.build(), context));

    } catch (IOException e1)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(path), path);
    } catch (SAXException e)
    {
      report.message(MessageId.RSC_005, EPUBLocation.create(path), e.getMessage());
    }
  }

}
