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

package com.adobe.epubcheck.overlay;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidators;

public class OverlayChecker implements ContentChecker, DocumentValidator
{

  private final OCFPackage ocf;
  private final Report report;
  private final String path;
  private final XRefChecker xrefChecker;
  private final GenericResourceProvider resourceProvider;
  private final EPUBVersion version;
  private final EPUBProfile profile;

  public OverlayChecker(OCFPackage ocf, Report report, String path,
      XRefChecker xrefChecker, EPUBVersion version) {
    this(ocf, report, path, xrefChecker, version, EPUBProfile.DEFAULT);
  }
  
  public OverlayChecker(OCFPackage ocf, Report report, String path,
      XRefChecker xrefChecker, EPUBVersion version, EPUBProfile profile)
  {
    this(ocf,ocf,report,path,xrefChecker,version,profile);
  }

  public OverlayChecker(String path,
      GenericResourceProvider resourceProvider, Report report, EPUBProfile profile)
  {
    this(null,resourceProvider,report,path,null,EPUBVersion.VERSION_3,profile);
  }
  
  private OverlayChecker(OCFPackage ocf, GenericResourceProvider resourceProvider, Report report, String path,
      XRefChecker xrefChecker, EPUBVersion version, EPUBProfile profile) {
    this.ocf = ocf;
    this.resourceProvider = resourceProvider;
    this.report = report;
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.version = version;
    this.profile = profile==null?EPUBProfile.DEFAULT:profile;
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
    int fatalErrorsSoFar = report.getFatalErrorCount();
    int errorsSoFar = report.getErrorCount();
    int warningsSoFar = report.getWarningCount();
    InputStream in = null;
    OverlayHandler overlayHandler;
    try
    {
      in = resourceProvider.getInputStream(path);
      XMLParser overlayParser = new XMLParser( ocf,
          in, path,
          "application/smil+xml", report, version);
      overlayHandler = new OverlayHandler(path, xrefChecker,
          overlayParser, report);
      overlayParser.addValidator(XMLValidators.MO_30_RNC.get());
      overlayParser.addValidator(XMLValidators.MO_30_SCH.get());
      overlayParser.addXMLHandler(overlayHandler);
      overlayParser.process();
    }
    catch (IOException e)
    {
      report.message(MessageId.RSC_001, new MessageLocation(this.ocf.getName(), -1, -1), path);
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
      catch (Exception ignored)
      {
      }
    }

    return fatalErrorsSoFar == report.getFatalErrorCount()
        && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }
}
