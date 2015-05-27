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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBProfile;
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
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.collect.ImmutableSet;

public class NavChecker implements ContentChecker, DocumentValidator
{
  private OCFPackage ocf;
  private final Report report;
  private final String path;
  private XRefChecker xrefChecker;
  private final String properties;
  private final String mimeType;
  private final EPUBVersion version;
  private final EPUBProfile profile;
  private final GenericResourceProvider resourceProvider;
  private final Set<String> pubTypes;

  public NavChecker(GenericResourceProvider resourceProvider, Report report,
      String path, String mimeType, EPUBVersion version, EPUBProfile profile)
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
    this.profile = profile==null?EPUBProfile.DEFAULT:profile;
    this.pubTypes = ImmutableSet.of();
  }

  public NavChecker(OCFPackage ocf, Report report, String path,
      String mimeType, String properties, XRefChecker xrefChecker, EPUBVersion version, Set<String> pubTypes, EPUBProfile profile)
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
    this.profile = profile==null?EPUBProfile.DEFAULT:profile;
    this.pubTypes = pubTypes;
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
    int fatalErrors = report.getFatalErrorCount();
    int errors = report.getErrorCount();
    int warnings = report.getWarningCount();
    InputStream in = null;
    try
    {
      in = resourceProvider.getInputStream(path);
      XMLParser navParser = new XMLParser(ocf, in, path,
          "application/xhtml+xml", report, version);

      XMLHandler navHandler = new OPSHandler30(ocf, path, mimeType,
          properties, xrefChecker, navParser, report, version, pubTypes, profile);
      navParser.addXMLHandler(navHandler);
      navParser.addValidator(XMLValidators.NAV_30_RNC.get());
      navParser.addValidator(XMLValidators.XHTML_30_SCH.get());
      navParser.addValidator(XMLValidators.NAV_30_SCH.get());
      navParser.process();
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_008, new MessageLocation(path, -1, -1), path);
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
      catch (IOException ignored)
      {
        // eat it
      }
    }

    return ((fatalErrors == report.getFatalErrorCount()) && (errors == report.getErrorCount()) && (warnings == report.getWarningCount()));
  }
}
