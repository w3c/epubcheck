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

import org.w3c.epubcheck.constants.MIMEType;
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

import io.mola.galimatias.URL;

public class XMLParser
{

  private static final String SAXPROP_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
  private static final String SAXPROP_DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler";
  private final ValidationContext context;
  private final Report report;
  private final URL url;
  private final SAXParser parser;
  private final DelegateDefaultHandler.Builder handler = new DelegateDefaultHandler.Builder();
  private boolean reporting = true;

  public XMLParser(ValidationContext context)
  {
    this.context = context;
    this.report = context.report;
    this.url = context.url;

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
    try (InputStream in = context.resourceProvider.openStream(context.url);
        InputStream buffered = new BufferedInputStream(in))
    {
      if (in == null)
      {
        // Abort processing.
        // Missing required files are reported elsewhere.
        return;
      }

      // Check encoding
      // If the result is null, the XML parser will must parse it as UTF-8
      String encoding = XMLEncodingSniffer.sniffEncoding(buffered);
      if (encoding != null && !encoding.equals("UTF-8"))
      {
        if (encoding.equals("UTF-16"))
        {
          // XHTML requires UTF-8, UTF-16 is reported as an error
          if (MIMEType.XHTML.is(context.mimeType))
          {
            report.message(MessageId.HTM_058, EPUBLocation.of(context));
          }
          // For other XML types, UTF-16 is reported as a warning
          else
          {
            report.message(MessageId.RSC_027, EPUBLocation.of(context));
          }
        }
        else
        {
          report.message(MessageId.RSC_028, EPUBLocation.of(context), encoding);
        }
      }

      // Build the input source
      // We do not set the source encoding name, but instead let the SAXParser
      // apply its own encoding-sniffing logic, as it can report useful errors
      // (for instance a mismatch between a BOM and the XML declaration)
      InputSource source = new InputSource(buffered);
      source.setSystemId(url.toString());

      // Set the error handler
      if (reporting)
      {
        handler.addErrorHandler(new ReportingErrorHandler(context));
      }

      // Parse
      parser.parse(source, new PreprocessingDefaultHandler(handler.build(), context));

    } catch (SAXAbortException e)
    {
      // Intentional parsing abort.
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.of(context), context.path);
    } catch (SAXException e)
    {
      // All errors should have already been reported by the error handler
      if (report.getFatalErrorCount() == 0)
      {
        report.message(MessageId.RSC_016, EPUBLocation.of(context), e.getMessage());
      }
    }
  }

}
