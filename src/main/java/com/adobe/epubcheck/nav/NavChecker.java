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

package com.adobe.epubcheck.nav;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.ops.OPSHandler30;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

import java.io.IOException;
import java.io.InputStream;

public class NavChecker implements ContentChecker, DocumentValidator
{
  private static final XMLValidator navValidator_30_RNC = new XMLValidator(
      "schema/30/epub-nav-30.rnc");

  private static final XMLValidator navValidator_30_ISOSCH = new XMLValidator(
      "schema/30/epub-nav-30.sch");

  private static final XMLValidator xhtmlValidator_30_ISOSCH = new XMLValidator(
      "schema/30/epub-xhtml-30.sch");

  private OCFPackage ocf;
  private final Report report;
  private final String path;
  private XRefChecker xrefChecker;
  private final String properties;
  private final String mimeType;
  private final EPUBVersion version;
  private final GenericResourceProvider resourceProvider;

  public NavChecker(GenericResourceProvider resourceProvider, Report report,
      String path, String mimeType, EPUBVersion version)
  {
    if (version == EPUBVersion.VERSION_2)
    {
      report.message(MessageId.NAV_001, new MessageLocation(path, 0, 0));
    }
    this.report = report;
    this.path = path;
    this.resourceProvider = resourceProvider;
    this.properties = "singleFileValidation";
    this.mimeType = mimeType;
    this.version = version;
  }

  public NavChecker(OCFPackage ocf, Report report, String path,
      String mimeType, String properties, XRefChecker xrefChecker, EPUBVersion version)
  {
    if (version == EPUBVersion.VERSION_2)
    {
      report.message(MessageId.NAV_001, new MessageLocation(path, 0, 0));
    }
    this.ocf = ocf;
    this.report = report;
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.resourceProvider = ocf;
    this.properties = properties;
    this.mimeType = mimeType;
    this.version = version;
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.RSC_001, new MessageLocation(this.ocf.getName(), -1, -1), path);
    }
    else if (!ocf.canDecrypt(path))
    {
      report.message(MessageId.RSC_004, new MessageLocation(this.ocf.getName(), 0, 0), path);
    }
    else
    {
      validate();
    }
  }

  public boolean validate()
  {
    int errors = report.getErrorCount();
    int warnings = report.getWarningCount();
    InputStream in = null;
    try
    {
      in = resourceProvider.getInputStream(path);
      XMLParser navParser = new XMLParser(ocf, in, path,
          "application/xhtml+xml", report, version);

      XMLHandler navHandler = new OPSHandler30(ocf, path, mimeType,
          properties, xrefChecker, navParser, report, version);
      navParser.addXMLHandler(navHandler);
      navParser.addValidator(navValidator_30_RNC);
      navParser.addValidator(xhtmlValidator_30_ISOSCH);
      navParser.addValidator(navValidator_30_ISOSCH);
      navParser.process();
    }
    catch (IOException e)
    {
      report.message(MessageId.RSC_005, new MessageLocation(path, -1, -1), e.getMessage());
    }
    finally
    {
      try
      {
        if (in != null)
        {
          in.close();
        }
      }
      catch (IOException e)
      {
        // eat it
      }
    }

    return ((errors == report.getErrorCount()) && (warnings == report.getWarningCount()));
  }
}
